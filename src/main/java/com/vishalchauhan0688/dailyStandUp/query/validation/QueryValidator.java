package com.vishalchauhan0688.dailyStandUp.query.validation;

import com.vishalchauhan0688.dailyStandUp.query.request.FilterCondition;
import com.vishalchauhan0688.dailyStandUp.query.request.QueryRequest;
import com.vishalchauhan0688.dailyStandUp.query.schema.ResourceSchema;
import lombok.AllArgsConstructor;
import lombok.Getter;



@AllArgsConstructor
@Getter
public class QueryValidator {
   public  ValidatiedQuery validateQuery(QueryRequest qr, ResourceSchema schema){

       for(FilterCondition filter : qr.getFilters()){
           if(!schema.getFields().containsKey(filter.getField())){
               throw new IllegalArgumentException(
                       "Unknown filter field: " + filter.getField()
               );
           }
       }
   }
}
