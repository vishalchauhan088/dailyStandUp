package com.vishalchauhan0688.dailyStandUp.query.validation;

import com.vishalchauhan0688.dailyStandUp.query.request.FilterCondition;
import com.vishalchauhan0688.dailyStandUp.query.request.QueryRequest;
import com.vishalchauhan0688.dailyStandUp.query.request.SortCondition;
import com.vishalchauhan0688.dailyStandUp.query.schema.FieldSchema;
import com.vishalchauhan0688.dailyStandUp.query.schema.ResourceSchema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.logging.Filter;

@Component
@AllArgsConstructor
@Getter
public class QueryValidator {
   public  ValidatiedQuery validateQuery(QueryRequest qr, ResourceSchema schema){

       List<FilterCondition> filters = qr.getFilters() == null ? List.of() : qr.getFilters();
       List<SortCondition> sorts = qr.getSorts() == null ? List.of() : qr.getSorts();

       for(FilterCondition filter : filters){
           //verify that field is valid
           if(!schema.getFields().containsKey(filter.getField())){
               throw new IllegalArgumentException(
                       "Unknown filter field: " + filter.getField()
               );
           }

           //verify that operator must be allowed
           FieldSchema field = schema.getFields().get(filter.getField());
           if(!field.getAllowedOperators().contains(filter.getOperator())){
               throw new IllegalArgumentException(
                       "Operator not allowed for field: " + filter.getField()
               );
           }

       }
       //verify that sorting is allowed
       for( SortCondition sc : sorts){
           if (!schema.getFields().containsKey(sc.getField())) {
               throw new IllegalArgumentException(
                       "Unknown sort field: " + sc.getField()
               );
           }
           if (!schema.getFields()
                   .get(sc.getField())
                   .isSortable()) {
               throw new IllegalArgumentException(
                       "Field not sortable: " + sc.getField()
               );
           }
       }

       //pagination bound
       int page = qr.getPage() == null ? 0 : qr.getPage();
       int size = qr.getSize() == null? schema.getDefaultPageSize(): qr.getSize();

       if(size > schema.getMaxPageSize()){
           throw new IllegalArgumentException("Page size exceeds maximum page size");
       }


       //create and return validated query
       return new ValidatiedQuery(
               filters,
               sorts,
               page,
               size
       );

   }
}
