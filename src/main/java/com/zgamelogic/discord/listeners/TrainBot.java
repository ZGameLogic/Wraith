package com.zgamelogic.discord.listeners;

import com.zgamelogic.annotations.DiscordController;
import com.zgamelogic.services.MetraService;
import lombok.AllArgsConstructor;

@DiscordController
@AllArgsConstructor
public class TrainBot {
    private final MetraService metraService;
}
