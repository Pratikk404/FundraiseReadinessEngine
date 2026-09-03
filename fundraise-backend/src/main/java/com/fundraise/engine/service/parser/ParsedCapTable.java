package com.fundraise.engine.service.parser;

import com.fundraise.engine.entity.EquityEvent;
import com.fundraise.engine.entity.ShareClass;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParsedCapTable {
    private List<EquityEvent> equityEvents;
    private List<ShareClass> shareClasses;
    private int totalRowsParsed;
    private List<String> warnings;
}
