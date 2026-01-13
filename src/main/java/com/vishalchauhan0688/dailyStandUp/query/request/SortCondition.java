package com.vishalchauhan0688.dailyStandUp.query.request;

import com.vishalchauhan0688.dailyStandUp.query.Direction;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SortCondition {
   private String field;
    private Direction direction;
}
