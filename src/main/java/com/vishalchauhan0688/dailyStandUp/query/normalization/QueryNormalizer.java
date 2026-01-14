package com.vishalchauhan0688.dailyStandUp.query.normalization;

import com.vishalchauhan0688.dailyStandUp.query.Direction;
import com.vishalchauhan0688.dailyStandUp.query.request.SortCondition;
import com.vishalchauhan0688.dailyStandUp.query.schema.ResourceSchema;
import com.vishalchauhan0688.dailyStandUp.query.validation.ValidatiedQuery;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * You can add default filters like only return isActive .
 */

@Component
public class QueryNormalizer {

    public NormalizedQuery normalize(ValidatiedQuery vq, ResourceSchema rc) {

        List<SortCondition> finalSorts = new ArrayList<>(vq.getSorts());

        //if no sorts use default sorts
        if(finalSorts.isEmpty()) {
            finalSorts.addAll(rc.getDefaultSort());
        }
        //Ensure tie breaker
        boolean hasIdSorts = finalSorts.stream()
                .anyMatch(s -> s.getField().equals("id"));
        //add id sort if sort has not id sort
        if(!hasIdSorts){
            finalSorts.add(new SortCondition("id", Direction.ASC));
        }

        return new NormalizedQuery(
                vq.getFilters(),
                finalSorts,
                vq.getPage(),
                vq.getSize()
        );


    }
}
