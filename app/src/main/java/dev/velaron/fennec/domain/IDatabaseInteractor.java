package dev.velaron.fennec.domain;

import java.util.List;

import dev.velaron.fennec.model.City;
import dev.velaron.fennec.model.database.Chair;
import dev.velaron.fennec.model.database.Country;
import dev.velaron.fennec.model.database.Faculty;
import dev.velaron.fennec.model.database.School;
import dev.velaron.fennec.model.database.SchoolClazz;
import dev.velaron.fennec.model.database.University;
import io.reactivex.Single;

/**
 * Created by Ruslan Kolbasa on 20.09.2017.
 * phoenix
 */
public interface IDatabaseInteractor {
    Single<List<Chair>> getChairs(int accoutnId, int facultyId, int count, int offset);
    Single<List<Country>> getCountries(int accountId, boolean ignoreCache);
    Single<List<City>> getCities(int accountId, int countryId, String q, boolean needAll, int count, int offset);
    Single<List<Faculty>> getFaculties(int accountId, int universityId, int count, int offset);
    Single<List<SchoolClazz>> getSchoolClasses(int accountId, int countryId);
    Single<List<School>> getSchools(int accountId, int cityId, String q, int count, int offset);
    Single<List<University>> getUniversities(int accoutnId, String filter, Integer cityId, Integer countyId, int count, int offset);
}