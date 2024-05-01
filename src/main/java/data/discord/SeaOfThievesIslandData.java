package data.discord;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import data.deserializers.sot.SeaOfThievesIslandDataDeserializer;

import java.io.File;

@JsonDeserialize(using = SeaOfThievesIslandDataDeserializer.class)
public record SeaOfThievesIslandData(File islandPng, String name, File areaIcon, String area, String chords) {}
