# PixelUI Engine
![](logo.png)

Java UI-System on top of LibGDX which provides a tiny 8x8-tile based Window and Component system
suited for low-resolution game and graphical applications.
This is a work in progress and subject to change - no maven release exists.

This library was used in the game [Sandtrix](https://www.sandtrix.net).

## Screenshots
![](screenshot_1.png)
![](screenshot_2.png)

## Features
- Windows
- Components
  - Button
  - Checkbox / Radiobox
  - Combobox
  - Image
  - Text
  - List
  - Grid
  - Knob
  - Canvas
  - Progressbar
  - Scrollbar
  - Shapes
  - Tabbars & Tabs
  - Textfield
  - AppViewPort
 - ToolTips
 - Context Menus
 - Notifications
 - Modals
 - Hotkeys
 - Drag & Drop
 - Keyboard, Gamepad & Mouse Input handling
 - Mouse emulation (Keyboard, Gamepad)
 - Gamepad & Touchscreen text inputs
 - Viewport handling / Pixel-Art upscaling
 - Asset-Management
 - High performance, low library size (~300kb)
 - Utility classes
  - Improved Sprite renderer
  - Primitive renderer (fast pixel rendering)
  - Particle system
  - Transition effects
  - Music & Sound players
  - Settings manager



## Overview
### desktop/ ... /example

Basic example producing the UI in the screenshot above showcasing a typical setup for the engine.

### core/ ... /engine.media_manager

This asset manager uses assets in the form of an internal CMedia descriptor format.
These assets can then be loaded at once and used/drawn via the SpriteRenderer.

The assets for the UI are contained in UIBaseMedia and need to be loaded alongside your own assets for the UI to work.

### core/ ... /engine.ui_engine

The core of the Engine. A class implementing the UIAdapter interfaces needs to be implemented and passed to a new UIEngine object. 

The UIEngine then passes a API Object into the Adapter on init() from which all windows, components can be created.

### Tools

These are not needed for the UI to work.
This package contains useful classes that integrate seamlessly with the Engine and use the internal formats and classes.

#### core.engine.tools/ ... AppEngine

Provides a basic framework for an engine which works in update cycles, provides input/output handling.
Uses the same Adapter approach as the UIEngine.

#### core.engine.tools/ ... JsonInlcudeParser

A json parser which supports include files via JSON comments.

#### core.engine.tools/ ... ParticleSystem

High performance particle systems using the MediaManager CMedia graphics formats and primitves.

#### core.engine.tools/ ... SettingsManager

A settings/options manager. Has failsafe functionality to ensure that all values are always valid.
A Implementation for reading/writing to java-.properties files is provided via the FileSettingsPersistor, custom storage methods can be implemented.

#### core.engine.tools/ ... SoundPlayer

A Soundplayer that uses the MediaManager and CMedia sound format.
This player supports playing sound in a virtual 2D space with automatic volume/pan adjustment based on distance and direction.

#### core.engine.tools/ ... MusicPlayer

A Musicplayer that uses MediaManager and CMedia music format.
This player works like you would expect it from a regular music player application, which means it supports playlists/shuffle/pause/resume/... etc.

#### core.engine.tools/ ... TransitionManager

A tool to create Transition effects when switching between two UIEngine instances

#### core.engine.tools/ ... Tools

Static helper & math functions.
