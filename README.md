# Flyway Edition
为了能够自定义处理不同的数据库，我做了如下修改：

[Flyway](./flyway-core/src/main/java/org/flywaydb/core/Flyway.java)中增加`DATABASE_MAPPING`用于储存自定义数据库类型的处理方式，

他会在[DatabaseFactory#createDatabase](./flyway-core/src/main/java/org/flywaydb/core/internal/database/DatabaseFactory.java)方法中执行原来的逻辑之前先判断

有没有匹配到，匹配到则返回自定义的Database类实例，否则执行原来的逻辑，

使用详情请参考Demo模块。

## Documentation
https://flywaydb.org

## How to contribute
https://flywaydb.org/documentation/contribute/

## About
Flyway is brought to you by [Axel Fontaine](https://twitter.com/axelfontaine) and [Boxfuse](https://boxfuse.com) with the help of [many contributors](https://flywaydb.org/documentation/contribute/hallOfFame.html).

## License
Copyright (C) 2010-2018 [Boxfuse GmbH](https://boxfuse.com)

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

## Trademark
Flyway is a registered trademark of [Boxfuse GmbH](https://boxfuse.com).
