package com.example.huqiang.retrofitdemo;


import org.reactivestreams.Publisher;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.FlowableTransformer;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by 83916 on 2018/1/1.
 */

public class RxHelper {

    /**
     * 切换线程操作
     * @return Observable转换器
     */
    public static <T> FlowableTransformer<T,T> transformer() {
        return new FlowableTransformer<T, T>() {
            @Override
            public Flowable<T> apply(Flowable<T> upstream) {
                return upstream
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread());
            }
        };
    }

    public static <T> ObservableTransformer<T,T> downloadTransformer(){
        return new ObservableTransformer<T, T>() {
            @Override
            public ObservableSource<T> apply(Observable<T> upstream) {
                return upstream
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread());
            }
        };
    }

   /* *//**   如果后台数据支持，则使用这个
     * 切换线程操作
     * 如果后台返回的数据格式规范的话，这里可以对数据进行预处理，根据ApiException来判断
     * @return Observable转换器
     */
   public static <T> FlowableTransformer<BaseResult<T>,T> handleResult(){
       return new FlowableTransformer<BaseResult<T>, T>() {
           @Override
           public Flowable<T> apply(Flowable<BaseResult<T>> upstream) {
               return upstream.flatMap(new Function<BaseResult<T>, Flowable<T>>() {
                   @Override
                   public Flowable<T> apply(BaseResult<T> tBaseResult) throws Exception {
                       if (tBaseResult.getCode()==0){
                           return createData(tBaseResult.getData());
                       }else {
                           return Flowable.error(new ApiException(tBaseResult.getCode()));
                       }
                   }
               });
           }
       };
   }
//    public static <T> ObservableTransformer<BaseResult<T>, T> handleResult() {
//        return new ObservableTransformer<BaseResult<T>, T>() {
//            @Override
//            public ObservableSource<T> apply(Observable<BaseResult<T>> upstream) {
//                return upstream.flatMap(new Function<BaseResult<T>, ObservableSource<T>>() {
//                    @Override
//                    public ObservableSource<T> apply(BaseResult<T> baseResult) {
//                        if (baseResult.getCode() == 0){
//                            return createData(baseResult.getData());
//                        }else {
//                            return Observable.error(new ApiException(baseResult.getCode()));
//                        }
//                    }
//                }).subscribeOn(Schedulers.io()).unsubscribeOn(Schedulers.io()).subscribeOn(AndroidSchedulers.mainThread()).observeOn(AndroidSchedulers.mainThread());
//            }
//        };
//        return new Observable.Transformer<BaseResult<T>, T>() {
//            @Override
//            public Observable<T> call(Observable<BaseResult<T>> tObservable) {
//                return tObservable.flatMap(new Func1<BaseResult<T>, Observable<T>>() {
//                    @Override
//                    public Observable<T> call(BaseResult<T> result) {
//                        if (result.getCode() == 0) {     //这里根据自己服务器的返回code逻辑来判断，我这里只是演示下
//                            return createData(result.getData());
//                        } else {
//                            return Observable.error(new ApiException(result.getCode()));
//                        }
//                    }
//                }).subscribeOn(Schedulers.io()).unsubscribeOn(Schedulers.io()).subscribeOn(AndroidSchedulers.mainThread()).observeOn(AndroidSchedulers.mainThread());
//            }
//        };
//    }

    /**
     * 创建成功的数据
     *
     * @param data
     * @param <T>
     * @return
     */
    private static <T> Flowable<T> createData(final T data) {
        return Flowable.create(new FlowableOnSubscribe<T>() {
            @Override
            public void subscribe(FlowableEmitter<T> emitter) {
                try {
                    emitter.onNext(data);
                    emitter.onComplete();
                }catch (Exception e){
                    emitter.onError(e);
                }
            }
        },BackpressureStrategy.BUFFER);
//        return Observable.unsafeCreate(new Observable.OnSubscribe<T>() {
//            @Override
//            public void call(Subscriber<? super T> subscriber) {
//                try {
//                    subscriber.onNext(data);
//                    subscriber.onCompleted();
//                } catch (Exception e) {
//                    subscriber.onError(e);
//                }
//            }
//        });
    }

}
