package com.hxbreak.animalcrossingtools.data.services;

import androidx.lifecycle.LiveData;

import com.hxbreak.animalcrossingtools.data.Result;
import com.hxbreak.animalcrossingtools.data.source.entity.Song;

import java.util.Map;

import retrofit2.http.GET;

public interface AnimalCrossingServices {

    @GET("songs/")
    LiveData<Result<Map<String, Song>>> allSongs();

}

