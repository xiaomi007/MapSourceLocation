package com.github.xiaomi007.maps;

import com.google.android.gms.common.api.GoogleApiClient;

/**
 * Created by xiaomi on 28/07/2016.
 */

public interface ClientObservable {

    void addObserver(ClientObserver clientObserver);

    void removeObserver(ClientObserver clientObserver);

    void notifyObserver(GoogleApiClient client);

}
