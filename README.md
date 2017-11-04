BotBuilder - Akka-Http
======================

This a non official, scala (more precisely an Akka-Http) implementation of the Microsoft's BotBuilder SDK.

Feel free to try it out (check the samples), use it, or contribute. The codebase is really far from a production ready solution right now, but it can grow up :D

- For more information check out the original repository with the official C# and Node.js implementation: https://github.com/Microsoft/BotBuilder
- You can check the official site too: https://dev.botframework.com/

If you want a feature, or try to help; start a new issue, or start a new PR.


##State of functions
- (+) basic message parsing and sending works (see Echo sample)
- (+) attachement send and receive most of the cases working (see the Attachement samples) (can't upload (just inline) and download from skype)
- (+) some cards have basic implementation (see the Cards sample)
- (+) can use and persist dialogs (MultipleDialog sample)
- (-) there are only a few preusable dialog (no Luis dialog)
- (-) only tested with the emulator (https://github.com/Microsoft/BotFramework-Emulator)
- (-) no tests :(

If you want to try this out just check out the source, write your credentials to the smaples/BaseSample, and start one of the samples (from your ide for example) and the emulator.


