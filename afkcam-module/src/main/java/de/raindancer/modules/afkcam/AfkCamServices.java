package de.raindancer.modules.afkcam;

import de.raindancer.core.ui.messages.Messages;
import de.raindancer.modules.afkcam.service.AfkCamService;

/** What this module built, handed to its command. */
public record AfkCamServices(Messages messages, AfkCamService cams) {
}
