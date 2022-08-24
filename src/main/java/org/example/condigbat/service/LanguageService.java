package org.example.condigbat.service;

import org.example.condigbat.payload.AddLanguageDTO;
import org.example.condigbat.payload.ApiResult;
import org.example.condigbat.payload.LanguageDTO;
import org.example.condigbat.payload.ViewDTO;
import org.example.condigbat.projection.LanguageDTOProjection;

import java.util.List;

public interface LanguageService {
    ApiResult<LanguageDTO> add(AddLanguageDTO addLanguageDTO);

    ApiResult<List<LanguageDTO>> getLanguages(ViewDTO viewDTO, int page, int size);

    ApiResult<LanguageDTO> getLanguage(Integer id);

    ApiResult<?> delete(Integer id);

    ApiResult<LanguageDTO> edit(AddLanguageDTO addLanguageDTO, Integer id);

    ApiResult<List<LanguageDTOProjection>> getLanguagesBySuperMethod(ViewDTO viewDTO, int page, int size);

    ApiResult<List<LanguageDTO>> getLanguagesForUser();

}
