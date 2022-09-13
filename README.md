# SimEngine 
Basis and GUI for a Simulation Game Engine using Libgdx


## Tools
### JSONIncludeParser
Parses JSON files with include statements.
- //INCLUDE <file> includes the file as is
- //INCLUDE_TRIM <file> includes file removing first and last line ({})

### LThreadPool
Threadpool that works on a list.

### MusicPlayer
Plays CMediaMusic Objects, supports playlists.

### SoundPlayer2D
Plays sounds and pans/adjusts volume based on a location in 2D space.

### Options
Class for Managing Game Options and sync them with a .properties file.
Supports validation and default values to ensure every critical value is set.

### Particle System
A particle System for 2D Particles. Extend Particle class and pass it to an instance of ParticleSystem.

### Registry
Sorts objects into lists sorted by type. 

### Tools
General utility class