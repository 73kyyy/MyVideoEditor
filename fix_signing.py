with open('app/build.gradle.kts') as f:
    content = f.read()

old_r = '''create("release") {
            storeFile = file("../keystore/nexclip.jks")
            storePassword = System.getenv("KEYSTORE_PASSWORD") ?: ""
            keyAlias = "nexclip"
            keyPassword = System.getenv("KEY_PASSWORD") ?: ""
        }'''
new_r = '''create("release") {
            storeFile = file("../keystore/nexclip.jks")
            storePassword = System.getenv("KEYSTORE_PASSWORD") ?: ""
            keyAlias = "nexclip"
            keyPassword = System.getenv("KEY_PASSWORD") ?: ""
            enableV1Signing = true
            enableV2Signing = true
            enableV3Signing = true
        }'''
old_d = '''getByName("debug") {
            storeFile = file("../keystore/debug.jks")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }'''
new_d = '''getByName("debug") {
            storeFile = file("../keystore/debug.jks")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
            enableV1Signing = true
            enableV2Signing = true
            enableV3Signing = true
        }'''
content = content.replace(old_r, new_r)
content = content.replace(old_d, new_d)
with open('app/build.gradle.kts', 'w') as f:
    f.write(content)
print("Done")
