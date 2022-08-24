package org.example.condigbat.service;

import lombok.RequiredArgsConstructor;
import org.example.condigbat.entity.Language;
import org.example.condigbat.entity.template.AbsIntegerEntity;
import org.example.condigbat.exceptions.RestException;
import org.example.condigbat.payload.*;
import org.example.condigbat.payload.enums.ConditionTypeEnum;
import org.example.condigbat.payload.enums.OperatorTypeEnum;
import org.example.condigbat.projection.LanguageDTOProjection;
import org.example.condigbat.repository.LanguageRepository;
import org.example.condigbat.repository.SectionRepository;
import org.example.condigbat.repository.UserProblemRepository;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LanguageServiceImpl implements LanguageService {


    private final MessageSource messageSource;
    private final LanguageRepository languageRepository;

    private final SectionRepository sectionRepository;

    private final UserProblemRepository userProblemRepository;

    @Override
    public ApiResult<LanguageDTO> add(AddLanguageDTO addLanguageDTO) {
        // todo LANGUAGE o'rniga LANGUAGE_DTO qaytarmiz

        if (languageRepository.existsByTitle(addLanguageDTO.getTitle()))
            throw RestException.restThrow("This language already exists", HttpStatus.CONFLICT);

        Language language = new Language();
        language.setTitle(addLanguageDTO.getTitle());

        languageRepository.save(language);

        LanguageDTO languageDTO = mapLanguageToLanguageDTO(language,
                0,
                0L,
                0L,
                0);

        return ApiResult.successResponse("Successfully saved", languageDTO);
    }

    @Override
    public ApiResult<List<LanguageDTO>> getLanguages(ViewDTO viewDTO,
                                                     int page,
                                                     int size) {

        //CLIENT JSON NULL BERSA YOKI SEARCH, FILTER, SORT QILMASA
        if (Objects.isNull(viewDTO)
                || (viewDTO.getFiltering().getColumns().isEmpty()
                && viewDTO.getSearching().getValue().isBlank()
                && viewDTO.getSorting().isEmpty())
        ) {
            Pageable pageable = PageRequest.of(
                    page,
                    size,
                    Sort.Direction.DESC, "title", "url"
            );

            Page<Language> languagePage = languageRepository.findAll(pageable);
            List<Language> languages = languagePage.getContent();

            List<LanguageDTO> languageDTOList = languages
                    .stream()
                    .map(this::mapLanguageToLanguageDTO)
                    .toList();
            return ApiResult.successResponse(languageDTOList);
        }

        Map<Integer, LanguageDTO> resultFilterMap = new HashMap<>();

        //CLIENT FILTER QILGANDA
        if (!viewDTO.getFiltering().getColumns().isEmpty()) {

            Map<Integer, Language> mapByTitle = new HashMap<>();//10
            Map<Integer, Language> mapByUrl = new HashMap<>();//8

            boolean filteringByTitle = false;
            boolean filteringByUrl = false;
            boolean filterByTryCount = false;
            boolean filterBySectionCount = false;
            boolean filterBySolutionCount = false;


            for (FilterColumnDTO column : viewDTO.getFiltering().getColumns()) {
                if (Objects.equals(column.getName(), "title")) {
                    filteringByTitle = true;
                    if (Objects.equals(column.getConditionType(), ConditionTypeEnum.IS_SET)) {
                        List<Language> languages = languageRepository.findAllByTitleIsNotNull();
                        mapLanguagesToHashMap(languages, mapByTitle);
                    } else if (Objects.equals(column.getConditionType(), ConditionTypeEnum.IS_NOT_SET)) {
                        List<Language> languages = languageRepository.findAllByTitleIsNull();
                        mapLanguagesToHashMap(languages, mapByTitle);
                    } else if (Objects.equals(column.getConditionType(), ConditionTypeEnum.CONTAINS)) {
                        List<Language> languages = languageRepository.findAllByTitleContainingIgnoreCase(column.getValue());
                        mapLanguagesToHashMap(languages, mapByTitle);
                    } else if (Objects.equals(column.getConditionType(), ConditionTypeEnum.NOT_CONTAINS)) {
                        List<Language> languages = languageRepository.findAllByTitleNotContainingIgnoreCase(column.getValue());
                        mapLanguagesToHashMap(languages, mapByTitle);
                    }
                } else if (Objects.equals(column.getName(), "url")) {
                    filteringByUrl = true;

                    if (Objects.equals(column.getConditionType(), ConditionTypeEnum.IS_SET)) {
                        List<Language> languages = languageRepository.findAllByUrlIsNotNull();
                        mapLanguagesToHashMap(languages, mapByUrl);
                    } else if (Objects.equals(column.getConditionType(), ConditionTypeEnum.IS_NOT_SET)) {
                        List<Language> languages = languageRepository.findAllByUrlIsNull();
                        mapLanguagesToHashMap(languages, mapByUrl);
                    } else if (Objects.equals(column.getConditionType(), ConditionTypeEnum.CONTAINS)) {
                        List<Language> languages = languageRepository.findAllByUrlContainingIgnoreCase(column.getValue());
                        mapLanguagesToHashMap(languages, mapByUrl);
                    } else if (Objects.equals(column.getConditionType(), ConditionTypeEnum.NOT_CONTAINS)) {
                        List<Language> languages = languageRepository.findAllByUrlNotContainingIgnoreCase(column.getValue());
                        mapLanguagesToHashMap(languages, mapByUrl);
                    }
                }
            }


            Map<Integer, Language> languageMap = new HashMap<>();//10

            if (Objects.equals(viewDTO.getFiltering().getOperatorType(), OperatorTypeEnum.AND)) {
                if (filteringByTitle & filteringByUrl)
                    mapByUrl.forEach((id, language) -> {
                        if (mapByTitle.containsKey(id))
                            languageMap.put(id, language);
                    });
                else {
                    languageMap.putAll(mapByTitle);
                    languageMap.putAll(mapByUrl);
                }
            } else {
                languageMap.putAll(mapByTitle);
                languageMap.putAll(mapByUrl);
            }

            Map<Integer, LanguageDTO> languageDTOMap = new HashMap<>();

            if (!(((filteringByUrl | filteringByTitle)
                    && !languageMap.isEmpty()
                    && Objects.equals(viewDTO.getFiltering().getOperatorType(), OperatorTypeEnum.AND))
            )) {
                List<Language> languages = languageRepository.findAll();

                mapLanguagesToHashMap(languages, languageMap);

            }
            mapLanguagesToHashMap(languageMap, languageDTOMap);

            Map<Integer, LanguageDTO> tryCountMap = new HashMap<>();
            Map<Integer, LanguageDTO> sectionCountMap = new HashMap<>();
            Map<Integer, LanguageDTO> solvedCountMap = new HashMap<>();

            for (FilterColumnDTO column : viewDTO.getFiltering().getColumns()) {
                if (Objects.equals(column.getName(), "tryCount")) {
                    filterByTryCount = true;
                    languageDTOMap.forEach((id, languageDTO) -> {
                        switch (column.getConditionType()) {
                            case EQ -> {
                                if (Objects.equals(languageDTO.getTryCount(), Long.valueOf(column.getValue())))
                                    tryCountMap.put(id, languageDTO);
                            }
                            case NOT_EQ -> {
                                if (!Objects.equals(languageDTO.getTryCount(), Long.valueOf(column.getValue())))
                                    tryCountMap.put(id, languageDTO);
                            }
                            case GT -> {
                                if (languageDTO.getTryCount() > Long.parseLong(column.getValue()))
                                    tryCountMap.put(id, languageDTO);
                            }
                            case LT -> {
                                if (languageDTO.getTryCount() < Long.parseLong(column.getValue()))
                                    tryCountMap.put(id, languageDTO);
                            }
                            case GTE -> {
                                if (languageDTO.getTryCount() >= Long.parseLong(column.getValue()))
                                    tryCountMap.put(id, languageDTO);
                            }
                            case LTE -> {
                                if (languageDTO.getTryCount() <= Long.parseLong(column.getValue()))
                                    tryCountMap.put(id, languageDTO);
                            }
                            case RA -> {
                                if (languageDTO.getTryCount() >= Long.parseLong(column.getFrom())
                                        && languageDTO.getTryCount() <= Long.parseLong(column.getTill()))
                                    tryCountMap.put(id, languageDTO);
                            }

                        }
                    });
                } else if (Objects.equals(column.getName(), "sectionCount")) {
                    filterBySectionCount = true;
                    languageDTOMap.forEach((id, languageDTO) -> {
                        switch (column.getConditionType()) {
                            case EQ -> {
                                if (Objects.equals(languageDTO.getSectionCount(), Integer.parseInt(column.getValue())))
                                    sectionCountMap.put(id, languageDTO);
                            }
                            case NOT_EQ -> {
                                if (!Objects.equals(languageDTO.getSectionCount(), Integer.parseInt(column.getValue())))
                                    sectionCountMap.put(id, languageDTO);
                            }
                            case GT -> {
                                if (languageDTO.getSectionCount() > Integer.parseInt(column.getValue()))
                                    sectionCountMap.put(id, languageDTO);
                            }
                            case LT -> {
                                if (languageDTO.getSectionCount() < Integer.parseInt(column.getValue()))
                                    sectionCountMap.put(id, languageDTO);
                            }
                            case GTE -> {
                                if (languageDTO.getSectionCount() >= Integer.parseInt(column.getValue()))
                                    sectionCountMap.put(id, languageDTO);
                            }
                            case LTE -> {
                                if (languageDTO.getSectionCount() <= Integer.parseInt(column.getValue()))
                                    sectionCountMap.put(id, languageDTO);
                            }
                            case RA -> {
                                if (languageDTO.getSectionCount() >= Integer.parseInt(column.getFrom())
                                        && languageDTO.getSectionCount() <= Integer.parseInt(column.getTill()))
                                    sectionCountMap.put(id, languageDTO);
                            }

                        }
                    });
                } else if (Objects.equals(column.getName(), "solvedCount")) {
                    filterBySolutionCount = true;
                    languageDTOMap.forEach((id, languageDTO) -> {
                        switch (column.getConditionType()) {
                            case EQ -> {
                                if (Objects.equals(languageDTO.getSolutionCount(), Long.parseLong(column.getValue())))
                                    solvedCountMap.put(id, languageDTO);
                            }
                            case NOT_EQ -> {
                                if (!Objects.equals(languageDTO.getSolutionCount(), Long.parseLong(column.getValue())))
                                    solvedCountMap.put(id, languageDTO);
                            }
                            case GT -> {
                                if (languageDTO.getSolutionCount() > Long.parseLong(column.getValue()))
                                    solvedCountMap.put(id, languageDTO);
                            }
                            case LT -> {
                                if (languageDTO.getSolutionCount() < Long.parseLong(column.getValue()))
                                    solvedCountMap.put(id, languageDTO);
                            }
                            case GTE -> {
                                if (languageDTO.getSolutionCount() >= Long.parseLong(column.getValue()))
                                    solvedCountMap.put(id, languageDTO);
                            }
                            case LTE -> {
                                if (languageDTO.getSolutionCount() <= Long.parseLong(column.getValue()))
                                    solvedCountMap.put(id, languageDTO);
                            }
                            case RA -> {
                                if (languageDTO.getSolutionCount() >= Long.parseLong(column.getFrom())
                                        && languageDTO.getSolutionCount() <= Long.parseLong(column.getTill()))
                                    solvedCountMap.put(id, languageDTO);
                            }

                        }
                    });
                }
            }


            if (Objects.equals(viewDTO.getFiltering().getOperatorType(), OperatorTypeEnum.AND)) {
                boolean finalFilterBySectionCount = filterBySectionCount;
                boolean finalFilterBySolutionCount = filterBySolutionCount;
                boolean finalFilterByTryCount = filterByTryCount;
                languageDTOMap.forEach((id, languageDTO) -> {
                    if (
                            (!finalFilterBySectionCount || sectionCountMap.containsKey(id))
                                    && (!finalFilterBySolutionCount || solvedCountMap.containsKey(id))
                                    && (!finalFilterByTryCount || tryCountMap.containsKey(id))
                    ) {
                        resultFilterMap.put(id, languageDTO);
                    }
                });
            } else {
                resultFilterMap.putAll(tryCountMap);
                resultFilterMap.putAll(solvedCountMap);
                resultFilterMap.putAll(sectionCountMap);
                resultFilterMap.putAll(languageDTOMap);
            }

        }

        //SEARCHING UCHUN
        if (
                !viewDTO.getFiltering().getColumns().isEmpty()
                        && !resultFilterMap.isEmpty()
        ) {
            //todo javada qidiramiz
        } else if (viewDTO.getFiltering().getColumns().isEmpty()) {
            //todo DB da qidiramiz
        } else if (resultFilterMap.isEmpty()) {
//            return ApiResult.successResponse();
        }


        return ApiResult.successResponse(new ArrayList<>(resultFilterMap.values()));
    }

    @Override
    public ApiResult<LanguageDTO> getLanguage(Integer id) {

        Language language = languageRepository.findById(id).orElseThrow(() ->
                RestException.restThrow("This id language not found", HttpStatus.NOT_FOUND));

        LanguageDTO languageDTO = mapLanguageToLanguageDTO(language);

        return ApiResult.successResponse(languageDTO);
    }


    @Override
    public ApiResult<?> delete(Integer id) {

        languageRepository.deleteById(id);

        return ApiResult.successResponse(messageSource.getMessage("SUCCESSFULLY_DELETED", null, LocaleContextHolder.getLocale()));
    }

    @Override
    public ApiResult<LanguageDTO> edit(AddLanguageDTO addLanguageDTO, Integer id) {

        Language language = languageRepository.findById(id).orElseThrow(() ->
                RestException.restThrow(
                        messageSource.getMessage("LANGUAGE_NOT_FOUND", null, LocaleContextHolder.getLocale()),
                        HttpStatus.NOT_FOUND));

        if (!Objects.equals(language.getTitle(), addLanguageDTO.getTitle())) {

            if (languageRepository.existsByTitle(addLanguageDTO.getTitle()))
                throw RestException.restThrow(
                        messageSource.getMessage("LANGUAGE_ALREADY_EXIST", null, LocaleContextHolder.getLocale()),
                        HttpStatus.CONFLICT
                );

            language.setTitle(addLanguageDTO.getTitle());
            languageRepository.save(language);
        }

        return ApiResult.successResponse(
                messageSource.getMessage("SUCCESSFULLY_EDITED", null, LocaleContextHolder.getLocale()),
                mapLanguageToLanguageDTO(language)
        );
    }

    @Override
    public ApiResult<List<LanguageDTOProjection>> getLanguagesBySuperMethod(
            ViewDTO viewDTO,
            int page,
            int size) {

        StringBuilder stringBuilder = new StringBuilder(
                "SELECT l.*," +
                        "       COUNT(s.id)                               AS section_count," +
                        "       COUNT(p.id)                               AS problem_count," +
                        "       COUNT(up.id)                              AS try_count," +
                        "       COUNT(CASE WHEN up.solved THEN up.id END) AS solution_count" +
                        " FROM language l" +
                        "         LEFT JOIN section s on l.id = s.language_id" +
                        "         LEFT JOIN problem p on s.id = p.section_id" +
                        "         LEFT JOIN user_problem up on p.id = up.problem_id"
        );
        if (Objects.isNull(viewDTO)
                || (viewDTO.getFiltering().getColumns().isEmpty()
                && viewDTO.getSearching().getValue().isBlank()
                && viewDTO.getSorting().isEmpty())
        ) {
            stringBuilder.append(" GROUP BY l.id, l.title " +
                    "ORDER BY title");
        }

        stringBuilder
                .append(" LIMIT ")
                .append(size)
                .append(" OFFSET ")
                .append(page * size);

        String query = stringBuilder.toString();
        List<LanguageDTOProjection> languagesByStringQuery = languageRepository.getLanguagesByStringQuery(query);
        List<LanguageDTOProjection> test = languageRepository.getTest();

        return ApiResult.successResponse(languagesByStringQuery);
    }


    @Override
    public ApiResult<List<LanguageDTO>> getLanguagesForUser() {
        List<Language> languages = languageRepository.findAll(Sort.by("title"));
        List<LanguageDTO> languageDTOList =
                languages.stream().map(language -> mapLanguageToLanguageDTO(language, false)).toList();
        return ApiResult.successResponse(languageDTOList);
    }

    private LanguageDTO mapLanguageToLanguageDTO(Language language) {
        long sectionCount = sectionRepository.countAllByLanguageId(language.getId());
        long tryCount = userProblemRepository.countAllByProblem_SectionLanguageIdJPQL(language.getId());
        long solvedCount = userProblemRepository.countAllBySolvedIsTrueAndProblem_SectionLanguageId(language.getId());
        //todo problem soni top
        return mapLanguageToLanguageDTO(language,
                Long.valueOf(sectionCount).intValue(),
                tryCount,
                solvedCount,
                0);
    }

    private LanguageDTO mapLanguageToLanguageDTO(Language language,
                                                 int sectionCount,
                                                 long tryCount,
                                                 long solvedCount,
                                                 int problemCount) {
        return new LanguageDTO(
                language.getId(),
                language.getTitle(),
                language.getUrl(),
                sectionCount,
                problemCount,
                tryCount,
                solvedCount);
    }

    private LanguageDTO mapLanguageToLanguageDTO(Language language, boolean forUser) {
        return new LanguageDTO(
                language.getId(),
                language.getTitle(),
                language.getUrl());
    }

    private void mapLanguagesToHashMap(List<Language> languages, Map<Integer, Language> map) {
        Map<Integer, Language> collect = languages
                .stream()
                .collect(Collectors.toMap(AbsIntegerEntity::getId, language -> language));

        map.putAll(collect);
    }

    private void mapLanguagesToHashMap(Map<Integer, Language> languageMap,
                                       Map<Integer, LanguageDTO> languageDTOMap) {
        languageMap.forEach((id, language) -> {
            languageDTOMap.put(
                    id,
                    mapLanguageToLanguageDTO(language)
            );
        });
    }
}
