plugins {
	alias(libs.plugins.android.application)
	alias(libs.plugins.kotlin.android)
	alias(libs.plugins.kotlin.compose)
	alias(libs.plugins.ksp)
}

android {
	namespace = "com.kidd1412.workcalendar"
	compileSdk = 34

	defaultConfig {
		applicationId = "com.kidd1412.workcalendar"
		minSdk = 24
		targetSdk = 34
		versionCode = 1
		versionName = "1.0"

		testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
	}

	buildTypes {
		release {
			isMinifyEnabled = false
			proguardFiles(
				getDefaultProguardFile("proguard-android-optimize.txt"),
				"proguard-rules.pro"
			)
		}
	}

	buildFeatures {
		compose = true
	}

	composeOptions {
		kotlinCompilerExtensionVersion = "1.5.15"
	}

	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_17
		targetCompatibility = JavaVersion.VERSION_17
	}

	kotlinOptions {
		jvmTarget = "17"
	}
}

kotlin {
	jvmToolchain(17)
}

dependencies {
	implementation(platform("androidx.compose:compose-bom:${libs.versions.composeBom.get()}"))
	implementation("androidx.activity:activity-compose:1.9.2")
	implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.6")
	implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.6")
	implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.6")
	implementation("androidx.compose.material3:material3")
	implementation("androidx.compose.ui:ui")
	implementation("androidx.compose.ui:ui-tooling-preview")

	implementation(libs.room.runtime)
	implementation(libs.room.ktx)
	ksp(libs.room.compiler)

	androidTestImplementation(platform("androidx.compose:compose-bom:${libs.versions.composeBom.get()}"))
	androidTestImplementation("androidx.compose.ui:ui-test-junit4")

	debugImplementation("androidx.compose.ui:ui-tooling")
	debugImplementation("androidx.compose.ui:ui-test-manifest")
}
