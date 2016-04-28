package com.softwinner.TvdFileManager;

import rx.Observable;
import rx.Observable.Transformer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class RxHelper {
	
	public static <T> Transformer<T, T> subscribeOnComputation() {
		return new Transformer<T, T>() {
			@Override
			public Observable<T> call(Observable<T> tObservable) {
				return tObservable.subscribeOn(Schedulers.computation())
						.observeOn(AndroidSchedulers.mainThread());
			}
		};
	}
}
