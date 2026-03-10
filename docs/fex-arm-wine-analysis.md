# Winlator（CMOD相关）FEX + ARM Wine 运行模式实现调研

## 调研范围
- `StevenMXZ/wine`（重点看 `compile.yml`）
- `Arihany/WinlatorWCPHub`
- `Pipetto-crypto/winlator`（重点看 `dev` 分支）

## 结论概览
在 `Pipetto-crypto/winlator` 的 `dev` 分支里，所谓 **FEX + ARM（ARM64EC）Wine 模式** 并不是“把整个 Wine 放在 FEX 里跑”，而是：

1. 先使用 ARM64EC 版 Wine（如 `proton-9.0-arm64ec`）作为主体。
2. 再在 `system32` 注入一个可切换的“32位仿真桥接 DLL”（`HODLL`）：
   - `libwow64fex.dll`（FEXCore 路径）
   - `wowbox64.dll`（WoWBox64 路径）
3. 启动时按容器配置 `emulator` 选择并设置 `HODLL`，由 Wine 的 ARM64EC/WoW64 机制调用对应桥接层。

也就是说，FEX 在这个设计里主要以 **Wine 侧的 WoW64/ARM64EC 扩展 DLL** 形式接入，而不是替代 Linux 用户态启动器的 `box64` 全链路方案。

---

## 关键实现点

### 1) ARM64EC Wine 构建来源
`StevenMXZ/wine` 的 workflow 直接以 `ARCH=aarch64` + `WINARCH=arm64ec` 构建（`build.sh --build`），说明 ARM64EC Wine 是单独产物链路。  

### 2) FEXCore / WoWBox64 作为内容包（WCP）
`Arihany/WinlatorWCPHub` 的流水线把两者都当成可发布内容包：
- `x86_fexcore.yml`：从 `FEX-Emu/FEX` 构建并打包 FEXCore WCP。
- `x86_wowbox64.yml`：构建 `wowbox64.dll` 并打包 WCP。
- profile 中明确 `type` 和挂载目标，FEXCore 类型最终用于注入 Wine 路径。

### 3) Winlator 中的容器模型支持“可选模拟器”
`Pipetto-crypto/winlator` 中：
- 容器字段有 `emulator`，默认是 `FEXCore`。
- 并存 `box64Version`、`fexcoreVersion`。
- UI 里 `emulator_entries` 为 `FEXCore` / `Box64`，且仅在 `wineInfo.isArm64EC()` 时展示对应 FEX 设置区域。

### 4) 启动前注入对应 DLL（最关键）
`BionicProgramLauncherComponent` 在 `wineInfo.isArm64EC()` 时调用 `extractEmulatorsDlls()`：
- 安装/解包 `wowbox64-*.tzst` 到 `system32`
- 安装/解包 `fexcore-*.tzst` 到 `system32`

启动命令分支：
- ARM64EC Wine：直接执行 `winePath/guestExecutable`
- 然后按 `emulator` 写入：
  - `HODLL=libwow64fex.dll`（FEXCore）
  - `HODLL=wowbox64.dll`（Box64/WoWBox64）

这说明运行时“切模拟器”的核心控制位就是 `HODLL`。

### 5) FEXCore 还有单独调优配置
`FEXCoreManager` 维护 `~/.fex-emu/Config.json` 和 AppConfig：
- TSO 模式
- Multiblock
- X87ReducedPrecision

这套配置通过 UI（Spinner）保存，启动前可生成 per-app 配置文件。

---

## 与传统 Box64 路径的差异

- **传统（非 ARM64EC）**：`box64 <guestExecutable>`，必要时配合 box86。
- **ARM64EC + FEX/WoWBox64**：不走外层 `box64 <wine>` 包裹；改为 ARM64EC Wine 直启 + `HODLL` 指向桥接 DLL。

因此该模式本质是“ARM64EC Wine + 可替换 WoW64 thunk/emulator DLL”。

---

## 分支观察
- `Pipetto-crypto/winlator` 可见分支：`dev`、`cmod-glibc-branch`、`winlator_bionic`。
- 与 FEX + ARM64EC 相关实现（容器字段、FEXCoreManager、Bionic 启动分支）在 `dev` 分支可直接定位。
- `cmod-glibc-branch` 的树中未明显看到同样的 FEX 关键词文件，推测该线并非当前主实现分支或实现路径不同。

