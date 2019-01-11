/*
 * Copyright 2018 Confluent Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.yokota.schemaregistry.mode;

import io.confluent.kafka.schemaregistry.rest.SchemaRegistryConfig;
import io.kcache.Cache;
import io.kcache.KafkaCache;
import io.kcache.KafkaCacheConfig;
import org.apache.kafka.common.serialization.Serdes;

import java.io.Closeable;
import java.io.IOException;

public class ModeRepository implements Closeable {

    // Used to represent all subjects
    public static final String SUBJECT_WILDCARD = "*";

    private final Cache<String, String> cache;

    public ModeRepository(SchemaRegistryConfig schemaRegistryConfig) {
        KafkaCacheConfig config =
            new KafkaCacheConfig(schemaRegistryConfig.originalProperties());
        cache = new KafkaCache<>(config, Serdes.String(), Serdes.String());
        cache.init();
    }

    public Mode getMode(String subject) {
        if (subject == null) subject = SUBJECT_WILDCARD;
        String mode = cache.get(subject);
        if (mode == null && subject.equals(SUBJECT_WILDCARD)) {
            // Default mode for top level
            return Mode.READWRITE;
        }
        return mode != null ? Enum.valueOf(Mode.class, mode) : null;
    }

    public void setMode(String subject, Mode mode) {
        if (subject == null) subject = SUBJECT_WILDCARD;
        cache.put(subject, mode.name());
    }

    @Override
    public void close() throws IOException {
        cache.close();
    }
}
