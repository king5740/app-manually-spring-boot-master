package org.example.condigbat.controller;

import org.example.condigbat.payload.AddLanguageDTO;
import org.example.condigbat.payload.ApiResult;
import org.example.condigbat.payload.LanguageDTO;
import org.example.condigbat.payload.ViewDTO;
import org.example.condigbat.projection.LanguageDTOProjection;
import org.example.condigbat.util.RestConstants;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;


@RequestMapping(path = "/language")
public interface LanguageController {

    @PostMapping(path = "/add")
    ApiResult<LanguageDTO> add(@Valid @RequestBody AddLanguageDTO addLanguageDTO);

    @PostMapping("/list")
    ApiResult<List<LanguageDTO>> getLanguages(@RequestBody(required = false) ViewDTO viewDTO,
                                              @RequestParam(defaultValue = RestConstants.DEFAULT_PAGE_NUMBER) int page,
                                              @RequestParam(defaultValue = RestConstants.DEFAULT_PAGE_SIZE) int size);

    @PostMapping("/super")
    ApiResult<List<LanguageDTOProjection>> getLanguagesBySuperMethod(
            @RequestBody(required = false) ViewDTO viewDTO,
            @RequestParam(defaultValue = RestConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = RestConstants.DEFAULT_PAGE_SIZE) int size);


    @GetMapping("/{id}")
    ApiResult<LanguageDTO> getLanguage(@PathVariable Integer id);

    @DeleteMapping("/{id}")
    ApiResult<?> delete(@PathVariable Integer id);

    @PutMapping("/{id}")
    ApiResult<LanguageDTO> edit(@Valid @RequestBody AddLanguageDTO addLanguageDTO,
                                @PathVariable Integer id);

    /**
     * USER YO'L
     */
    @GetMapping("/list-for-users")
    public ApiResult<List<LanguageDTO>> getLanguagesForUser();

}
