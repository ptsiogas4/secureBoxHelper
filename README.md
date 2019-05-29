# secureBoxHelper
This is a simple Kotlin library in order to easily encrypt/decrypt a String with a given password.

### Building
=======================
Fork the repository and include the 'library' module and you are done :)

Or use JitPack: https://jitpack.io/#ptsiogas4/secureBoxHelper

```
repositories {
    maven { url 'https://jitpack.io' }
}
dependencies {
    implementation 'com.github.ptsiogas4:secureBoxHelper:0.0.3'
}
```

## secureBoxHelper kotlin sample code
```kotlin
SecureBoxHelper.instance.encryptString("testVar", "sampleText", "mySecurePassword")
SecureBoxHelper.instance.decryptString("testVar", "mySecurePassword")
OR
SecureBoxHelper.instance.encryptString("testVar", "sampleText")
SecureBoxHelper.instance.decryptString("testVar")

SecureBoxHelper.instance.deleteString("testVar")
SecureBoxHelper.instance.deleteAllStrings()
```

## Developed by
  ptsiogas - <a href='javascript:'>ptsiogas@gmail.com</a>

## License
	Copyright [2019] ptsiogas

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
