package rip.diamond.practice.layout;

import io.github.epicgo.sconey.element.SconeyElement;
import io.github.epicgo.sconey.element.SconeyElementAdapter;
import io.github.epicgo.sconey.element.SconeyElementMode;
import org.bukkit.entity.Player;
import rip.diamond.practice.Eden;
import rip.diamond.practice.Language;
import rip.diamond.practice.events.EdenEvent;
import rip.diamond.practice.match.Match;
import rip.diamond.practice.party.Party;
import rip.diamond.practice.profile.PlayerProfile;
import rip.diamond.practice.profile.PlayerState;
import rip.diamond.practice.profile.ProfileSettings;
import rip.diamond.practice.queue.Queue;
import rip.diamond.practice.queue.QueueProfile;
import rip.diamond.practice.queue.QueueType;

public class ScoreboardAdapter implements SconeyElementAdapter {

    private final Eden plugin = Eden.INSTANCE;

    /**
     * This method returns the scoreboard element used by this instance
     * @param player the player containing the provided scoreboard
     * @return the scoreboard element used by this instance
     */
    @Override
    public SconeyElement getElement(final Player player) {
        SconeyElement element = new SconeyElement();

        element.setTitle(Language.SCOREBOARD_TITLE.toString(player));
        element.setMode(SconeyElementMode.CUSTOM);

        PlayerProfile profile = PlayerProfile.get(player);

        if (profile == null) {
            return element;
        }

        Party party = Party.getByPlayer(player);
        QueueProfile qProfile = Queue.getPlayers().get(player.getUniqueId());
        Match match = profile.getMatch();
        EdenEvent event = EdenEvent.getOnGoingEvent();

        if (profile.getPlayerState() == PlayerState.LOADING) {
            element.addAll(Language.SCOREBOARD_LOADING.toStringList(player));
        } else if (profile.getPlayerState() == PlayerState.IN_LOBBY && party == null) {
            element.addAll(Language.SCOREBOARD_IN_LOBBY.toStringList(player));
        } else if (profile.getPlayerState() == PlayerState.IN_LOBBY && party != null) {
            element.addAll(Language.SCOREBOARD_IN_PARTY.toStringList(player));
        } else if (profile.getPlayerState() == PlayerState.IN_EDIT) {
            element.addAll(Language.SCOREBOARD_IN_EDIT.toStringList(player));
        } else if (profile.getPlayerState() == PlayerState.IN_QUEUE && qProfile != null && qProfile.getQueueType() == QueueType.UNRANKED) {
            element.addAll(Language.SCOREBOARD_IN_QUEUE_UNRANKED.toStringList(player));
        } else if (profile.getPlayerState() == PlayerState.IN_QUEUE && qProfile != null && qProfile.getQueueType() == QueueType.RANKED) {
            element.addAll(Language.SCOREBOARD_IN_QUEUE_RANKED.toStringList(player));
        } else if (event != null && event.getTotalPlayers().contains(player) && event.getInGameScoreboard(player) != null) {
            element.addAll(event.getInGameScoreboard(player));
        } else if (profile.getPlayerState() == PlayerState.IN_MATCH && match != null) {
            if (!profile.getSettings().get(ProfileSettings.MATCH_SCOREBOARD).isEnabled()) {
                return element;
            }
            element.addAll(match.getMatchScoreboard(player));
        } else if (profile.getPlayerState() == PlayerState.IN_SPECTATING && match != null) {
            if (!profile.getSettings().get(ProfileSettings.MATCH_SCOREBOARD).isEnabled()) {
                return element;
            }
            element.addAll(match.getSpectateScoreboard(player));
        }
        
        return element;
    }
}