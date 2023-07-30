package com.gdn.tms.routing.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
/*TODO:Absorbing only needed fields. Have to add others */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OSRMTimeResponse {
    String code;
    long[][] durations;
}
