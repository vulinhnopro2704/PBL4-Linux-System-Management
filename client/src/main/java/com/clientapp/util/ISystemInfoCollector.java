package com.clientapp.util;

import com.clientapp.model.ClientDetail;

import java.util.List;

public interface ISystemInfoCollector {
    /**
     *
     * @return A ClientDetail Object containing Client Detail information.
     */
    ClientDetail getClientDetail();
}
