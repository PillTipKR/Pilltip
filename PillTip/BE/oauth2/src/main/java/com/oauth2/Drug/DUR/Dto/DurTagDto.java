package com.oauth2.Drug.DUR.Dto;

import java.util.List;

// DUR 태그 객체
public record DurTagDto(
   String title,
   List<DurDto> durDtos,
   boolean isTrue
) {}
