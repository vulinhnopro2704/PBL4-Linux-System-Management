package com.serverapp.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class ClientDetail extends ClientCard {
    private String cpuModel;
    private Long ram;
    private Long usedDisk;
    private Long totalDisk;
    private List<ClientProcess> processDetails;
}
