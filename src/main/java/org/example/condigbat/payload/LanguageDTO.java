package org.example.condigbat.payload;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import org.example.condigbat.projection.LanguageDTOProjection;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LanguageDTO {

    private Integer id;

    private String title;

    private String url;

    private Integer sectionCount;

    private Integer problemCount;

    private Long tryCount;

    private Long solutionCount;

    public LanguageDTO(Integer id, String title, String url) {
        this.id = id;
        this.title = title;
        this.url = url;
    }
}
