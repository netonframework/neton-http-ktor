# Independent compatibility adapter

Extracted from Neton Framework commit d2d176b3deb3c2b51e55aee92f0fbf9d765a9a05.
Source and compiled API baseline are retained; this is not a new default engine.

This repository owns its tests, ABI baseline and release lifecycle. It is not part
of the Neton BOM or core release gate. Its Maven coordinate remains
`com.netonstream:neton-http-ktor`; no previously published version may be overwritten.
The default dependency baseline is Neton 1.0.0-beta25 (Kotlin 2.4.20).
Set `-PnetonVersion=...` to test another explicitly supported release.

Run `./gradlew macosArm64Test` (Linux: `linuxX64Test`, Windows: `mingwX64Test`).
Conformance test coverage is platform-specific, as before extraction.
`./gradlew checkKotlinAbi` validates all compiled target API baselines on macOS.

For an isolated candidate repository, pass `-Pneton.repository=/absolute/path`
and `-PnetonVersion=...`. Normal builds never probe sibling checkouts.
Explicit source development uses `-Pneton.local=true`; publishing rejects it.
Local packaging uses `publishAllPublicationsToStagingLocalRepository`.
Production publication/signing is not configured by this migration.

## 中文

本仓库是可选的兼容和引擎对比适配器，不属于 Neton 核心发布门禁或 BOM。
默认使用已发布的 Neton 契约，不隐式加载相邻源码。自研 I/O 协议栈仍是
Neton 唯一的内置默认实现。迁移不改变调用入口，不代表发布新版本。
