package com.zgamelogic.discord.listeners;

import com.zgamelogic.annotations.DiscordController;
import com.zgamelogic.data.database.trains.TrainRepository;
import lombok.AllArgsConstructor;

@DiscordController
@AllArgsConstructor
public class TrainBot {
    private final TrainRepository trainRepository;
}
