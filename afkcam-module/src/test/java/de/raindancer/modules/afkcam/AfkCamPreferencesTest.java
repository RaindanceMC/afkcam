package de.raindancer.modules.afkcam;

import de.raindancer.core.ui.messages.Messages;
import de.raindancer.core.ui.preference.Preference;
import de.raindancer.core.ui.profile.PlayerSwitch;
import de.raindancer.modules.afkcam.service.AfkCamService;
import de.raindancer.modules.afkcam.util.PermissionNodes;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AfkCamPreferencesTest {

    private final Player player = mock(Player.class);
    private final Messages messages = mock(Messages.class);
    private final PlayerSwitch wanted = mock(PlayerSwitch.class);
    private AfkCamSettings settings = AfkCamSettings.DEFAULTS;
    private Preference preference;

    @BeforeEach
    void wire() {
        AfkCamService cams = mock(AfkCamService.class);
        when(cams.wanted()).thenReturn(wanted);
        AfkCamServices services = new AfkCamServices(messages, cams);
        preference = AfkCamPreferences.camera(() -> services, () -> settings);
    }

    @Test
    void isTheCameraSwitchAndGoesThroughIt() {
        when(wanted.isOn(player)).thenReturn(true);
        assertThat(preference.id()).isEqualTo("afkcam:camera");
        assertThat(preference.state(player)).isEqualTo("On");

        preference.choose().accept(player, 1);
        verify(wanted).set(player, false);
        verify(messages).send(player, "afkcam.switched-off");
        preference.choose().accept(player, 0);
        verify(wanted).set(player, true);
        verify(messages).send(player, "afkcam.switched-on");
    }

    @Test
    void notOfferedWhenTheServerTurnedTheCameraOffOrWithoutPermission() {
        when(player.hasPermission(PermissionNodes.USE)).thenReturn(true);
        assertThat(preference.shown().test(player)).isTrue();
        when(player.hasPermission(PermissionNodes.USE)).thenReturn(false);
        assertThat(preference.shown().test(player)).isFalse();
        when(player.hasPermission(PermissionNodes.USE)).thenReturn(true);
        settings = new AfkCamSettings(false, true, 5, 20, 4, 1, 20, true, true, "custom", true);
        assertThat(preference.shown().test(player)).isFalse();
    }
}
