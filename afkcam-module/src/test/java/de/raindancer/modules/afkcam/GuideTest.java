package de.raindancer.modules.afkcam;

import de.raindancer.core.ui.help.HelpLint;
import de.raindancer.modules.api.ModuleCommand;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class GuideTest {

    @Test
    void theGuideIsComplete() {
        assertThat(HelpLint.of(Path.of("src/main/resources/rains-help"))
                .module("afkcam")
                .settings(AfkCamSettings.class, AfkCamSettings.DEFAULTS)
                .commands(new AfkCamModule().commands().stream().map(ModuleCommand::name).toList())
                .problems()).isEmpty();
    }
}
