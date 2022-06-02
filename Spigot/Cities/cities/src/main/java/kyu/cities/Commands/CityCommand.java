package kyu.cities.Commands;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;

import Kyu.SCommand;
import kyu.cities.Main;
import kyu.cities.Util.City.City;
import kyu.cities.Util.City.CityOption;
import kyu.cities.Util.City.CityRank;
import kyu.cities.Util.City.EntryRequirement;
import kyu.cities.Util.General.Constants;
import kyu.cities.Util.General.Pair;
import kyu.cities.Util.Player.CPlayer;
import net.kyori.adventure.text.Component;

public class CityCommand {

    public static Map<CPlayer, Pair<Consumer<Void>, Integer>> confirmTasks = new HashMap<>();

    public static void init() {
        SCommand cityCmd = new SCommand(Main.getInstance(), "city", Main.helper);
        cityCmd.playerOnly(true);
        cityCmd.minArgs(1);
        cityCmd.execPerm("cities.city");
        cityCmd.exec(e -> {
            CPlayer p = CPlayer.players.get(e.player());

            // #region confirm command

            if (e.args()[0].equalsIgnoreCase("confirm")) {
                handleConfirm(p);
            }

            // #endregion confirm command

            // #region info command without args
            if (e.args()[0].equalsIgnoreCase("info") && e.args().length == 1) {
                if (p.getCity() == null) {
                    p.sendMessage(Main.helper.getMess(e.player(), "MustBeInCityForCMDExtended", true));
                    return;
                }
                City city = p.getCity();
                sendInfoCommand(p, city.getName());
                return;
            }
            // #endregion info command without args

            // #region leave command
            if (e.args()[0].equalsIgnoreCase("leave")) {
                if (p.getCity() == null) {
                    p.sendMessage(Main.helper.getMess(e.player(), "MustBeInCityForCMD", true));
                    return;
                }

                if (p.getRank().equals(CityRank.MAYOR)) {
                    p.sendMessage(Main.helper.getMess(e.player(), "CannotLeaveOwnCity", true));
                    return;
                }

                p.leaveCity();

                p.sendMessage(Main.helper.getMess(e.player(), "LeftCity", true));
                return;
            }
            // #endregion leave command

            // #region delete command
            if (e.args()[0].equalsIgnoreCase("delete")) {
                if (p.getCity() == null) {
                    p.sendMessage(Main.helper.getMess(e.player(), "MustBeInCityForCMD", true));
                    return;
                }

                if (!p.getRank().equals(CityRank.MAYOR)) {
                    p.sendMessage(Main.helper.getMess(e.player(), "RankTooLow", true));
                    return;
                }

                addConfirmTask(new Consumer<Void>() {
                    @Override
                    public void accept(Void t) {
                        p.sendMessage(Main.helper.getMess(e.player(), "DeletedCity", true));
                        p.getCity().delete();
                    }
                }, p);
                p.sendMessage(Main.helper.getMess(e.player(), "ConfirmDeleteCity", true));
                return;
            }
            // #endregion delete command

            // #region claim command
            if (e.args()[0].equalsIgnoreCase("claim")) {
                if (p.getCity() == null) {
                    p.sendMessage(Main.helper.getMess(e.player(), "MustBeInCityForCMD", true));
                    return;
                }
                City city = p.getCity();

                if (p.getRank().getVal() < city.getMinClaimRank().getVal()) {
                    p.sendMessage(Main.helper.getMess(e.player(), "RankTooLow", true));
                    return;
                }

                if (!city.canClaimChunks()) {
                    p.sendMessage(Main.helper.getMess(e.player(), "CityCannotClaimMoreChunks", true));
                    return;
                }

                if (City.isChunkOwned(e.player().getChunk()) != null) {
                    p.sendMessage(Main.helper.getMess(e.player(), "ChunkAlreadyOwned", true));
                    return;
                }
                city.claimChunk(e.player().getChunk());
                p.sendMessage(Main.helper.getMess(e.player(), "ChunkClaimed", true));
                return;
            }
            // #endregion claim command

            //#region showRequests command

            if (e.args()[0].equalsIgnoreCase("showRequests")) {
                if (p.getCity() == null) {
                    p.sendMessage(Main.helper.getMess(e.player(), "MustBeInCityForCMD", true));
                    return;
                }

                if (p.getRank().getVal() < CityRank.CITY_COUNCIL.getVal()) {
                    p.sendMessage(Main.helper.getMess(e.player(), "RankTooLow", true));
                    return;
                }

                boolean showEmptyMessage = true;
                p.getCity().displayJoinRequests(p, showEmptyMessage);
                return;
            }

            //#endregion showRequests command

            if (e.args().length < 2) {
                e.player().sendMessage(Component.text(Main.helper.getMess(e.player(), "NEArgs", true)));
                return;
            }

            // #region transfer command

            if (e.args()[0].equalsIgnoreCase("transfer")) {

                if (p.getCity() == null) {
                    p.sendMessage(Main.helper.getMess(e.player(), "MustBeInCityForCMD", true));
                    return;
                }

                if (p.getRank().getVal() < CityRank.MAYOR.getVal()) {
                    p.sendMessage(Main.helper.getMess(e.player(), "RankTooLow", true));
                    return;
                }

                String targetName = e.args()[1];
                YamlConfiguration mapper = Main.getInstance().getNameMapperConfig();
                if (mapper.getString("Names." + targetName) == null) {
                    p.sendMessage(Main.helper.getMess(e.player(), "NoPlayerFound", true));
                    return;
                }

                UUID targetUUID = UUID.fromString(mapper.getString("Names." + targetName));

                if (!CPlayer.getCityName(targetUUID).equalsIgnoreCase(p.getCity().getName())) {
                    p.sendMessage(Main.helper.getMess(e.player(), "PlayerNotInSameCity", true));
                    return;
                }

                addConfirmTask(new Consumer<Void>() {
                    @Override
                    public void accept(Void t) {
                        CPlayer.setRank(targetUUID, CityRank.MAYOR);
                        p.setRank(CityRank.CITY_COUNCIL);

                        p.sendMessage(Main.helper.getMess(e.player(), "TransferredCityTo", true)
                                .replace("%player%", targetName));
                    }
                }, p);

                p.sendMessage(Main.helper.getMess(e.player(), "ConfirmTransferCity", true)
                        .replace("%player%", targetName));
                return;
            }

            // #endregion transfer command

            // #region promote command
            if (e.args()[0].equalsIgnoreCase("promote")) {

                if (p.getCity() == null) {
                    p.sendMessage(Main.helper.getMess(e.player(), "MustBeInCityForCMD", true));
                    return;
                }

                String playerName = e.args()[1];
                YamlConfiguration nameMapper = Main.getInstance().getNameMapperConfig();

                if (nameMapper.getString(playerName) == null) {
                    p.sendMessage(Main.helper.getMess(e.player(), "PlayerNotFound", true));
                    return;
                }

                UUID uuid = UUID.fromString(nameMapper.getString(playerName));
                if (!CPlayer.getCityName(uuid).equalsIgnoreCase(p.getCity().getName())) {
                    p.sendMessage(Main.helper.getMess(e.player(), "PlayerNotInSameCity", true));
                    return;
                }

                CityRank targetRank = CPlayer.getCityRank(uuid);

                if (p.getRank().getVal() < CityRank.CITY_COUNCIL.getVal()
                        || targetRank.getVal() + 1 == p.getRank().getVal()) {
                    p.sendMessage(Main.helper.getMess(e.player(), "RankTooLow", true));
                    return;
                }

                if (targetRank.getVal() + 1 == CityRank.MAYOR.getVal()) {
                    p.sendMessage(Main.helper.getMess(e.player(), "CannotPromoteToMayor", true));
                    return;
                }

                CityRank newRank = CityRank.values()[targetRank.getVal() + 1];

                CPlayer.setRank(uuid, newRank);

                p.sendMessage(Main.helper.getMess(e.player(), "PlayerPromoted", true)
                        .replace("%playerName", playerName)
                        .replace("%newRank", Main.helper.getMess(e.player(), newRank.toString(), false)));
                return;

            }
            // #endregion promote command

            // #region kick command
            if (e.args()[0].equalsIgnoreCase("kick")) {
                String playerName = e.args()[1];
                if (p.getCity() == null) {
                    p.sendMessage(Main.helper.getMess(e.player(), "MustBeInCityForCMD", true));
                    return;
                }
                City city = p.getCity();
                if (p.getRank().getVal() < CityRank.CITY_COUNCIL.getVal()) {
                    p.sendMessage(Main.helper.getMess(e.player(), "RankTooLow", true));
                    return;
                }

                // Check target player, if mayor, return, if same rank don't kick
                YamlConfiguration mapper = Main.getInstance().getNameMapperConfig();
                if (mapper.get(playerName.toLowerCase()) == null) {
                    p.sendMessage(Main.helper.getMess(e.player(), "PlayerNotFoundInMapper", true));
                    return;
                }
                UUID targetPUuid = UUID.fromString(mapper.getString(playerName));
                if (CPlayer.getCityName(targetPUuid) == null
                        || !CPlayer.getCityName(targetPUuid).equalsIgnoreCase(p.getCity().getName())) {
                    p.sendMessage(Main.helper.getMess(e.player(), "PlayerNotInSameCity", true));
                    return;
                }
                CityRank targetRank = CPlayer.getCityRank(targetPUuid);
                if (targetRank.equals(CityRank.MAYOR)) {
                    p.sendMessage(Main.helper.getMess(e.player(), "CannotKickMayor", true));
                    return;
                }
                if (targetRank.equals(p.getRank())) {
                    p.sendMessage(Main.helper.getMess(e.player(), "CannotKickSameRank", true));
                    return;
                }

                city.removePlayer(targetPUuid);
                CPlayer.removeCity(targetPUuid);

                Map<String, String> replace = new HashMap<>();
                replace.put("%city%", city.getName());
                CPlayer.sendOfflineMess(playerName, "YouWereKickedFromCity", replace, true);
                p.sendMessage(Main.helper.getMess(e.player(), "PlayerKicked", true).replace("%player", playerName));
                return;
            }
            // #endregion kick command

            // #region acceptInvite/denyInvite command
            if (e.args()[0].equalsIgnoreCase("acceptinvite") || e.args()[0].equalsIgnoreCase("denyinvite")) {
                String cityName = e.args()[1];
                if (!City.exists(cityName)) {
                    p.sendMessage(Main.helper.getMess(e.player(), "CityDoesNotExist", true));
                    return;
                }
                if (p.getCity() != null) {
                    p.sendMessage(Main.helper.getMess(e.player(), "AlreadyInACity", true));
                    return;
                }
                if (e.args()[0].equalsIgnoreCase("acceptInvite")) {
                    City.joinCity(p, cityName);
                    p.sendMessage(Main.helper.getMess(e.player(), "CityJoined", true)
                            .replace("%name", cityName));
                } else {
                    p.sendMessage(Main.helper.getMess(e.player(), "InviteDenied", true));
                }
                City.removeInvite(p.getPlayer().getUniqueId(), cityName);
                return;
            }
            // #endregion acceptInvite/denyInvite command

            // #region revoke invite command
            if (e.args()[0].equalsIgnoreCase("revokeInv")) {

                if (p.getCity() == null) {
                    p.sendMessage(Main.helper.getMess(e.player(), "MustBeInCityForCMD", true));
                    return;
                }

                if (p.getRank().getVal() < CityRank.CITY_COUNCIL.getVal()) {
                    p.sendMessage(Main.helper.getMess(e.player(), "RankTooLow", true));
                    return;
                }

                String playerName = e.args()[1];
                YamlConfiguration mapper = Main.getInstance().getNameMapperConfig();
                if (mapper.get(playerName.toLowerCase()) == null) {
                    p.sendMessage(Main.helper.getMess(e.player(), "PlayerNotFoundInMapper", true));
                    return;
                }

                UUID targetPUuid = UUID.fromString(mapper.getString(playerName));
                if (!p.getCity().hasInviteFor(targetPUuid)) {
                    p.sendMessage(Main.helper.getMess(e.player(), "NoInviteFound", true));
                    return;
                }

                p.getCity().removeInvite(targetPUuid);
                p.sendMessage(Main.helper.getMess(e.player(), "InviteRevoked", true));
                return;
            }
            // #endregion revoke invite command

            // #region invite command
            if (e.args()[0].equalsIgnoreCase("invite")) {

                if (p.getCity() == null) {
                    p.sendMessage(Main.helper.getMess(e.player(), "MustBeInCityForCMD", true));
                    return;
                }

                String playerName = e.args()[1];
                YamlConfiguration nameMapper = Main.getInstance().getNameMapperConfig();
                if (nameMapper.get(playerName.toLowerCase()) == null) {
                    p.sendMessage(Main.helper.getMess(e.player(), "PlayerNotFoundInMapper", true));
                    return;
                }

                String uuid = nameMapper.getString(playerName.toLowerCase());
                if (CPlayer.isInCity(UUID.fromString(uuid))) {
                    p.sendMessage(Main.helper.getMess(e.player(), "PlayerAlreadyInACity", true));
                    return;
                }

                if (p.getCity().hasInviteFor(UUID.fromString(uuid))) {
                    p.sendMessage(Main.helper.getMess(e.player(), "PlayerAlreadyInvited", true));
                    return;
                }

                p.getCity().addInvite(UUID.fromString(uuid));

                Map<String, String> replaceValues = new HashMap<>();
                replaceValues.put("%name", p.getCity().getName());
                CPlayer.sendOfflineMess(playerName, "GotInvitedToCity", replaceValues, true);
                p.sendMessage(Main.helper.getMess(e.player(), "InvitedPlayerToCity", true)
                        .replace("%pName", playerName)
                        .replace("%name", p.getCity().getName()));
                return;
            }
            // #endregion invite command

            // #region JoinRequests
            if (e.args()[0].equalsIgnoreCase("allow") || e.args()[0].equalsIgnoreCase("deny")) {
                if (p.getCity() == null) {
                    p.sendMessage(Main.helper.getMess(e.player(), "MustBeInCityForCMD", true));
                    return;
                }
                String playerName = e.args()[1];
                YamlConfiguration cityConf = Main.getInstance().getCitiesConfig();
                City city = p.getCity();

                if (!(p.getRank().equals(CityRank.MAYOR) || p.getRank().equals(CityRank.CITY_COUNCIL))) {
                    p.sendMessage(Main.helper.getMess(e.player(), "RankNotHighEnough", true));
                    return;
                }

                if (cityConf.get(city.getName().toLowerCase() + ".joinRequests") == null
                        || !cityConf.getStringList(city.getName().toLowerCase() + ".joinRequests")
                                .contains(playerName.toLowerCase())) {
                    p.sendMessage(Main.helper.getMess(e.player(), "NoJoinRequestFromPlayer", true));
                    return;
                }

                city.removeJoinRequest(playerName);

                YamlConfiguration playerConf = Main.getInstance().getPlayersConfig();

                if (e.args()[0].equalsIgnoreCase("allow")) {
                    YamlConfiguration nameMapper = Main.getInstance().getNameMapperConfig();
                    if (nameMapper.get(playerName.toLowerCase()) == null) {
                        p.sendMessage(Main.helper.getMess(e.player(), "PlayerNotFoundInMapper", true));
                        return;
                    }
                    String uuid = nameMapper.getString(playerName.toLowerCase());
                    if (playerConf.get(uuid + ".city") != null) { // Player is already in a city
                        p.sendMessage(Main.helper.getMess(e.player(), "PlayerAlreadyInACity", true));
                        return;
                    }

                    if (CPlayer.isOnline(UUID.fromString(uuid))) {
                        CPlayer joiner = CPlayer.players.get(Bukkit.getPlayer(UUID.fromString(uuid)));
                        City.joinCity(joiner, p.getCity().getName());
                    } else {
                        playerConf.set(uuid + ".city", p.getCity().getName());
                        Main.saveConfig(playerConf);
                    }

                    Map<String, String> replaceValues = new HashMap<>();
                    replaceValues.put("%name", city.getName());
                    CPlayer.sendOfflineMess(playerName, "joinRequestGotAccepted", replaceValues, true);
                    p.sendMessage(Main.helper.getMess(e.player(), "joinRequestAccepted", true)
                            .replace("%name", playerName));
                } else {
                    Map<String, String> replaceValues = new HashMap<>();
                    replaceValues.put("%name", city.getName());
                    CPlayer.sendOfflineMess(playerName, "joinRequestGotDenied", replaceValues, true);
                    p.sendMessage(Main.helper.getMess(e.player(), "joinRequestDenied", true)
                            .replace("%name", playerName));
                }
                return;
            }
            // #endregion JoinRequests

            // #region join command
            if (e.args()[0].equals("join")) {
                String cityName = e.args()[1];
                YamlConfiguration cityConfig = Main.getInstance().getCitiesConfig();
                if (cityConfig.get(cityName.toLowerCase()) == null) {
                    p.sendMessage(Main.helper.getMess(e.player(), "CityDoesNotExist", true));
                    return;
                }
                EntryRequirement req = EntryRequirement
                        .valueOf(cityConfig.getString(cityName.toLowerCase() + ".entryReq"));
                switch (req) {
                    case INVITE_ONLY:
                        p.sendMessage(Main.helper.getMess(e.player(), "YouMustBeInvited", true));
                        break;
                    case NONE:
                        City.joinCity(p, cityName);
                        p.setRank(CityRank.NEW_MEMBER);
                        p.sendMessage(Main.helper.getMess(e.player(), "CityJoined", true).replace("%name", cityName));
                        break;
                    case REQUEST:
                        City.requestJoin(p, cityName);
                        break;
                }
            }
            // #endregion join command

            // #region info command with args
            if (e.args()[0].equals("info")) {
                String cityName = e.args()[1];
                YamlConfiguration cityConfig = Main.getInstance().getCitiesConfig();
                if (cityConfig.get(cityName.toLowerCase()) == null) {
                    p.sendMessage(Main.helper.getMess(e.player(), "CityDoesNotExist", true));
                    return;
                }
                sendInfoCommand(p, cityName);
                return;
            }
            // #endregion info commadn with args

            // #region create command
            if (e.args()[0].equalsIgnoreCase("create")) {
                if (p.getCity() != null) {
                    p.sendMessage(Main.helper.getMess(e.player(), "AlreadyInACity", true));
                    return;
                }

                String cityName = e.args()[1];
                YamlConfiguration cityConfig = Main.getInstance().getCitiesConfig();
                if (cityConfig.get(cityName.toLowerCase()) != null) {
                    p.sendMessage(Main.helper.getMess(e.player(), "CityNameTaken", true));
                    return;
                }
                City city = new City(cityName);
                city.initNew(e.player().getUniqueId());
                p.setCity(city);
                p.setRank(CityRank.MAYOR);
                p.sendMessage(Main.helper.getMess(e.player(), "CityCreated", true).replace("%name", cityName));
                return;
            }
            // #endregion create command

            if (e.args().length < 3) {
                p.sendMessage(Main.helper.getMess(e.player(), "NEArgs", true));
                return;
            }

            // #region options command

            if (e.args()[0].equalsIgnoreCase("options")) {

                if (p.getCity() == null) {
                    p.sendMessage(Main.helper.getMess(e.player(), "NotInACity", true));
                    return;
                }

                if (p.getRank().getVal() < p.getCity().getMinEditRank().getVal()) {
                    p.sendMessage(Main.helper.getMess(e.player(), "RankTooLow", true));
                    return;
                }

                String option = e.args()[1];

                CityOption cityOption = CityOption.valueOf(option);
                if (cityOption == null) {
                    p.sendMessage(Main.helper.getMess(e.player(), "InvalidOption", true));
                    return;
                }

                String value = e.args()[2];
                if (!cityOption.isValidValue(value)) {
                    p.sendMessage(Main.helper.getMess(e.player(), "InvalidValue", true));
                    return;
                }

                p.getCity().setOption(cityOption, value);
                p.sendMessage(Main.helper.getMess(e.player(), "OptionSet", true)
                        .replace("%option", cityOption.toString().toLowerCase())
                        .replace("%value", value));
                return;
            }

            // #endregion options command
        });
    }

    private static void sendInfoCommand(CPlayer p, String cityName) {
        YamlConfiguration cityConfig = Main.getInstance().getCitiesConfig();
        String mayorName = Bukkit
                .getOfflinePlayer(UUID.fromString(cityConfig.getString(cityName.toLowerCase() + ".mayor"))).getName();
        StringBuilder cityCouncilNames = new StringBuilder();
        int councilCount = 0;
        StringBuilder fullMemberNames = new StringBuilder();
        int fullMemberCount = 0;
        StringBuilder newMemberNames = new StringBuilder();
        int newMemberCount = 0;
        for (String uuid : cityConfig.getStringList(cityName.toLowerCase() + ".cityCouncil")) {
            cityCouncilNames.append((Bukkit.getOfflinePlayer(UUID.fromString(uuid))).getName()).append(", ");
            councilCount++;
        }
        for (String uuid : cityConfig.getStringList(cityName.toLowerCase() + ".fullMembers")) {
            fullMemberNames.append((Bukkit.getOfflinePlayer(UUID.fromString(uuid))).getName()).append(", ");
            fullMemberCount++;
        }
        for (String uuid : cityConfig.getStringList(cityName.toLowerCase() + ".newMembers")) {
            newMemberNames.append((Bukkit.getOfflinePlayer(UUID.fromString(uuid))).getName()).append(", ");
            newMemberCount++;
        }
        // Remove the ", " at the end of the Strings
        cityCouncilNames = new StringBuilder(
                cityCouncilNames.substring(0, cityCouncilNames.length() - 2 < 0 ? 0 : cityCouncilNames.length() - 2));
        fullMemberNames = new StringBuilder(
                fullMemberNames.substring(0, fullMemberNames.length() - 2 < 0 ? 0 : fullMemberNames.length() - 2));
        newMemberNames = new StringBuilder(
                newMemberNames.substring(0, newMemberNames.length() - 2 < 0 ? 0 : newMemberNames.length() - 2));
        int level = City.getLevel(cityConfig.getDouble(cityName.toLowerCase() + ".exp"));
        p.sendMessage(Main.helper.getMess(p.getPlayer(), "CityInfo", true)
                .replace("%name", cityConfig.getString(cityName.toLowerCase() + ".caseSensitiveName"))
                .replace("%level", "" + level)
                .replace("%entryReq",
                        Main.helper.getMess(p.getPlayer(), cityConfig.getString(cityName.toLowerCase() + ".entryReq")))
                .replace("%mayor", mayorName)
                .replace("%councilCount", councilCount + "")
                .replace("%council", cityCouncilNames.toString())
                .replace("%fullMembers", fullMemberNames.toString())
                .replace("%fullCount", fullMemberCount + "")
                .replace("%newMembers", newMemberNames.toString())
                .replace("%newCount", newMemberCount + ""));
    }

    private static void handleConfirm(CPlayer p) {
        if (!confirmTasks.containsKey(p))
            return;
        Pair<Consumer<Void>, Integer> pair = confirmTasks.get(p);
        pair.first.accept(null);
        Bukkit.getScheduler().cancelTask(pair.second);
        confirmTasks.remove(p);
    }

    private static void addConfirmTask(Consumer<Void> function, CPlayer p) {
        if (confirmTasks.containsKey(p)) {
            confirmTasks.remove(p);
        }
        int taskId = Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getInstance(), () -> {
            confirmTasks.remove(p);
            p.sendMessage(Main.helper.getMess(p.getPlayer(), "ConfirmTimedOut", true));
        }, 20 * Constants.confirmTimeout);
        confirmTasks.put(p, new Pair<Consumer<Void>, Integer>(function, taskId));
    }

}
