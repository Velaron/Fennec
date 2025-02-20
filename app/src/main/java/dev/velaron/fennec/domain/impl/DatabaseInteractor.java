package dev.velaron.fennec.domain.impl;

import java.util.ArrayList;
import java.util.List;

import dev.velaron.fennec.api.interfaces.INetworker;
import dev.velaron.fennec.api.model.VKApiCity;
import dev.velaron.fennec.api.model.VKApiCountry;
import dev.velaron.fennec.api.model.database.ChairDto;
import dev.velaron.fennec.api.model.database.FacultyDto;
import dev.velaron.fennec.api.model.database.SchoolClazzDto;
import dev.velaron.fennec.api.model.database.SchoolDto;
import dev.velaron.fennec.api.model.database.UniversityDto;
import dev.velaron.fennec.db.interfaces.IDatabaseStore;
import dev.velaron.fennec.db.model.entity.CountryEntity;
import dev.velaron.fennec.domain.IDatabaseInteractor;
import dev.velaron.fennec.model.City;
import dev.velaron.fennec.model.database.Chair;
import dev.velaron.fennec.model.database.Country;
import dev.velaron.fennec.model.database.Faculty;
import dev.velaron.fennec.model.database.School;
import dev.velaron.fennec.model.database.SchoolClazz;
import dev.velaron.fennec.model.database.University;
import dev.velaron.fennec.util.Utils;
import io.reactivex.Single;

/**
 * Created by Ruslan Kolbasa on 20.09.2017.
 * phoenix
 */
public class DatabaseInteractor implements IDatabaseInteractor {

    private final IDatabaseStore cache;
    private final INetworker networker;

    public DatabaseInteractor(IDatabaseStore cache, INetworker networker) {
        this.cache = cache;
        this.networker = networker;
    }

    @Override
    public Single<List<Chair>> getChairs(int accoutnId, int facultyId, int count, int offset) {
        return networker.vkDefault(accoutnId)
                .database()
                .getChairs(facultyId, offset, count)
                .map(items -> {
                    List<ChairDto> dtos = Utils.listEmptyIfNull(items.getItems());
                    List<Chair> chairs = new ArrayList<>(dtos.size());

                    for(ChairDto dto : dtos){
                        chairs.add(new Chair(dto.id, dto.title));
                    }

                    return chairs;
                });
    }

    @Override
    public Single<List<Country>> getCountries(int accountId, boolean ignoreCache) {
        if(ignoreCache){
            return networker.vkDefault(accountId)
                    .database()
                    .getCountries(true, null, null, 1000)
                    .flatMap(items -> {
                        List<VKApiCountry> dtos = Utils.listEmptyIfNull(items.getItems());
                        List<CountryEntity> dbos = new ArrayList<>(dtos.size());
                        List<Country> countries = new ArrayList<>(dbos.size());

                        for(VKApiCountry dto : dtos){
                            dbos.add(new CountryEntity(dto.id, dto.title));
                            countries.add(new Country(dto.id, dto.title));
                        }

                        return cache.storeCountries(accountId, dbos)
                                .andThen(Single.just(countries));
                    });
        }

        return cache.getCountries(accountId)
                .flatMap(dbos -> {
                    if(dbos.size() > 0){
                        List<Country> countries = new ArrayList<>(dbos.size());
                        for(CountryEntity dbo : dbos){
                            countries.add(new Country(dbo.getId(), dbo.getTitle()));
                        }

                        return Single.just(countries);
                    }

                    return getCountries(accountId, true);
                });
    }

    @Override
    public Single<List<City>> getCities(int accountId, int countryId, String q, boolean needAll, int count, int offset) {
        return networker.vkDefault(accountId)
                .database()
                .getCities(countryId, null, q, needAll, offset, count)
                .map(items -> {
                    List<VKApiCity> dtos = Utils.listEmptyIfNull(items.getItems());
                    List<City> cities = new ArrayList<>(dtos.size());

                    for(VKApiCity dto : dtos){
                        cities.add(new City(dto.id, dto.title)
                        .setArea(dto.area)
                        .setImportant(dto.important)
                        .setRegion(dto.region));
                    }

                    return cities;
                });
    }

    @Override
    public Single<List<Faculty>> getFaculties(int accountId, int universityId, int count, int offset) {
        return networker.vkDefault(accountId)
                .database()
                .getFaculties(universityId, offset, count)
                .map(items -> {
                    List<FacultyDto> dtos = Utils.listEmptyIfNull(items.getItems());
                    List<Faculty> faculties = new ArrayList<>(dtos.size());

                    for(FacultyDto dto : dtos){
                        faculties.add(new Faculty(dto.id, dto.title));
                    }

                    return faculties;
                });
    }

    @Override
    public Single<List<SchoolClazz>> getSchoolClasses(int accountId, int countryId) {
        return networker.vkDefault(accountId)
                .database()
                .getSchoolClasses(countryId)
                .map(dtos -> {
                    List<SchoolClazz> clazzes = new ArrayList<>(dtos.size());

                    for(SchoolClazzDto dto : dtos){
                        clazzes.add(new SchoolClazz(dto.id, dto.title));
                    }

                    return clazzes;
                });
    }

    @Override
    public Single<List<School>> getSchools(int accountId, int cityId, String q, int count, int offset) {
        return networker.vkDefault(accountId)
                .database()
                .getSchools(q, cityId, offset, count)
                .map(items -> {
                    List<SchoolDto> dtos = Utils.listEmptyIfNull(items.getItems());
                    List<School> schools = new ArrayList<>(dtos.size());

                    for(SchoolDto dto : dtos){
                        schools.add(new School(dto.id, dto.title));
                    }

                    return schools;
                });
    }

    @Override
    public Single<List<University>> getUniversities(int accoutnId, String filter, Integer cityId, Integer countyId, int count, int offset) {
        return networker.vkDefault(accoutnId)
                .database()
                .getUniversities(filter, countyId, cityId, offset, count)
                .map(items -> {
                    List<UniversityDto> dtos = Utils.listEmptyIfNull(items.getItems());
                    List<University> universities = new ArrayList<>(dtos.size());

                    for(UniversityDto dto : dtos){
                        universities.add(new University(dto.id, dto.title));
                    }

                    return universities;
                });
    }
}