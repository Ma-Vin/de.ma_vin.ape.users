package de.ma_vin.ape.users.controller;

import static de.ma_vin.ape.utils.controller.response.ResponseUtil.*;

import de.ma_vin.ape.users.model.gen.domain.IIdentifiable;
import de.ma_vin.ape.users.model.gen.dto.ITransportable;
import de.ma_vin.ape.utils.controller.response.ResponseWrapper;

import java.util.Optional;


public abstract class AbstractDefaultOperationController {

    /**
     * Gets a Dto object from Repository
     *
     * @param identification Identification of the dto which is searched for
     * @param domainClass    Corresponding domain class of the dto
     * @param searcher       Functional to access the domain object
     * @param converter      Functional to convert the domain object to a dto one
     * @param <T>            Domain class of the object which searched for
     * @param <S>            Corresponding dto of the domain class
     * @return ResponseWrapper containing the search result, if no error occurs
     */
    protected <T extends IIdentifiable, S extends ITransportable> ResponseWrapper<S> get(String identification
            , Class<T> domainClass, Searcher<T> searcher, Converter<T, S> converter) {
        return get(identification, domainClass, searcher, converter, Boolean.FALSE, null);
    }

    /**
     * Gets a Dto object from Repository
     *
     * @param identification    Identification of the dto which is searched for
     * @param domainClass       Corresponding domain class of the dto
     * @param searcher          Functional to access the domain object
     * @param converter         Functional to convert the domain object to a dto one
     * @param loadSubEntities   true if the sub entities of the domain object should also be loaded
     * @param subEntitySearcher Searcher of sub entities whose also adding to parent
     * @param <T>               Domain class of the object which searched for
     * @param <S>               Corresponding dto of the domain class
     * @return ResponseWrapper containing the search result, if no error occurs
     */
    protected <T extends IIdentifiable, S extends ITransportable> ResponseWrapper<S> get(String identification
            , Class<T> domainClass, Searcher<T> searcher, Converter<T, S> converter, Boolean loadSubEntities, SubEntitySearcher<T> subEntitySearcher) {

        Optional<T> searchedDomainObject = searcher.find(identification);
        if (searchedDomainObject.isEmpty()) {
            return createEmptyResponseWithError(String.format("There is not any %s with identification \"%s\"", domainClass.getSimpleName(), identification));
        }

        if (Boolean.TRUE.equals(loadSubEntities) && subEntitySearcher != null) {
            subEntitySearcher.findAndAdd(searchedDomainObject.get());
        }

        S result = converter.convert(searchedDomainObject.get());

        return createSuccessResponse(result);
    }

    /**
     * Deletes an exiting instance
     *
     * @param identification Identification of the instance to delete
     * @param domainClass    Corresponding domain class of the identification
     * @param searcher       Functional to access the domain object
     * @param deleter        Functional to delete the domain model object
     * @param <T>            the domain model which should be deleted
     * @return ResponseWrapper containing a {@link Boolean} whether the instance was deleted or not, if no error occurs
     */
    protected <T extends IIdentifiable> ResponseWrapper<Boolean> delete(String identification, Class<T> domainClass, Searcher<T> searcher, Deleter<T> deleter, ExistenceChecker existenceChecker) {

        Optional<T> persistedDomainObject = searcher.find(identification);
        if (persistedDomainObject.isEmpty()) {
            return createResponseWithWarning(Boolean.FALSE, String.format("There is not any %s with identification \"%s\" which could be deleted"
                    , domainClass.getSimpleName(), identification));
        }

        deleter.delete(persistedDomainObject.get());

        return existenceChecker.exists(identification)
                ? createEmptyResponseWithFatal(String.format("The %s \"%s\" could not be deleted for some unexpected reason", domainClass.getSimpleName(), identification))
                : createSuccessResponse(Boolean.TRUE);
    }

    /**
     * Updates an existing instance
     *
     * @param inputDto          Dto containing the modified data
     * @param identification    Identification of the instance to update
     * @param domainClass       Corresponding domain class of the dto
     * @param searcher          Functional to access the domain object
     * @param storer            Functional to store the updated domain model instance
     * @param toDtoConverter    Functional to convert the domain model object to an dto one
     * @param toDomainConverter Functional to convert the dto object to an domain model one
     * @param <T>               the domain model which should be updated
     * @param <S>               corresponding dto model which should be updated
     * @return ResponseWrapper containing the updated instance, if no error occurs
     */
    protected <T extends IIdentifiable, S extends ITransportable> ResponseWrapper<S> update(S inputDto, String identification
            , Class<T> domainClass, Searcher<T> searcher, Storer<T> storer, Converter<T, S> toDtoConverter, Converter<S, T> toDomainConverter) {

        Optional<T> persistedDomainObject = searcher.find(identification);
        if (persistedDomainObject.isEmpty()) {
            return createEmptyResponseWithError(String.format("There is not any %s with identification \"%s\" which could be updated"
                    , domainClass.getSimpleName(), identification));
        }

        T modifiedDomainObject = toDomainConverter.convert(inputDto);
        modifiedDomainObject.setIdentification(identification);

        Optional<T> storedDomainObject = storer.save(modifiedDomainObject);
        if (storedDomainObject.isEmpty()) {
            return createEmptyResponseWithFatal(String.format("The %s \"%s\" could not be modified for some unexpected reason"
                    , domainClass.getSimpleName(), identification));
        }

        S result = toDtoConverter.convert(storedDomainObject.get());
        if (inputDto.getIdentification().equals(identification)) {
            return createSuccessResponse(result);
        }
        return createResponseWithWarning(result, String.format("The identification of the request body \"%s\" was different to the path variable \"%s\". The path variable was used."
                , inputDto.getIdentification(), identification));
    }

    @FunctionalInterface
    protected interface Searcher<T extends IIdentifiable> {
        Optional<T> find(String identification);
    }

    @FunctionalInterface
    protected interface Deleter<T extends IIdentifiable> {
        void delete(T toDelete);
    }

    @FunctionalInterface
    protected interface ExistenceChecker {
        boolean exists(String identificationToCheck);
    }

    @FunctionalInterface
    protected interface Storer<T extends IIdentifiable> {
        Optional<T> save(T toStore);
    }

    @FunctionalInterface
    protected interface Converter<T, S> {
        S convert(T toConvert);
    }

    @FunctionalInterface
    protected interface SubEntitySearcher<T extends IIdentifiable> {
        void findAndAdd(T parent);
    }
}
