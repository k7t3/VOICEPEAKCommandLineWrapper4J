/*
 * Copyright 2024 k7t3
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.k7t3.voicepeakcw4j;

import java.util.concurrent.Flow;
import java.util.function.Consumer;

/**
 * {@link Flow.Subscriber}インターフェースを継承したユーティリティインタフェース
 * @param <T> 購読するタイプ
 */
@FunctionalInterface
public interface Subscriber<T> extends Flow.Subscriber<T> {

    /**
     * {@link Flow.Subscriber}の実装を生成するコンビニエンスメソッド
     * @param consumer 購読する関数
     * @return サブスクライバーの実装
     * @param <T> 購読するタイプ
     */
    static <T> Subscriber<T> of(Consumer<T> consumer) {
        return consumer::accept;
    }

    @Override
    default void onSubscribe(Flow.Subscription subscription) {
        subscription.request(Long.MAX_VALUE);
    }

    @Override
    default void onComplete() {
        // no-op
    }

    @Override
    default void onError(Throwable throwable) {
        System.getLogger(getClass().getName()).log(System.Logger.Level.ERROR, "error occurred", throwable);
    }

}
