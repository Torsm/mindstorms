# Mindstorms (experimental)
### Multithreaded communication and execution framework for EV3 blocks

Bots, a.k.a. classes that actually handle process flow and logic of the robot, are Runnables implemented in the ```de.thkoeln.mindstorms.bots``` package, tagged with the ```@MindstormsBot``` annotation. To request the robot to do anything, an instance of ```EV3Controller``` is passed to each bot's constructor (see ```DummyBot.java``` for reference).

The ```EV3Controller``` interface is a stub for the client to resolve opcodes. On the server side, the interface is implemented to execute the requests on the EV3 block. On the client side a proxy object will forward the method calls to a service that handles communication.

All methods in that controller return an ```ObservableRequest``` in order to be able to design a bot both event-driven and concurrent. For example you may want to read sensor data while a request to travel a longer distance is still pending.

All data transferred between the client and server are formatted in bytes (in contrast to json or similar formats), which may be stupidly overkill but was a fun thing to implement, just as the whole framework itself really.

### Future

I will hopefully add proper code documentation. Most of this was written entirely different than it was planned, and in the heat of the moment when my ideas kept sparking I must have forgotten about it or something like that.

Also proper exception handling would be nice I guess.

I could also make the whole communication framework bidirectional, in order to react to events that occur on the EV3 block's side, such as button presses or exceptions. 