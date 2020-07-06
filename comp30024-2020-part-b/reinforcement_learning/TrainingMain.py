"""
Replacement for __main__ in referee in order to run consecutive games for training
"""

from referee.log import StarLog
from referee.RunGame import play, IllegalActionException
from referee.player import PlayerWrapper, ResourceLimitException, set_space_line
from referee.options import get_options
from FledglingAI import NeuralNet

def main():
    # Parse command-line options into a namespace for use throughout this
    # program
    options = get_options()

    # Create a star-log for controlling the format of output from within this
    # program
    out = StarLog(level=options.verbosity, ansi=options.use_colour)
    out.comment("all messages printed by the referee after this begin with a *")
    out.comment("(any other lines of output must be from your Player classes).")
    out.comment()

    try:
        # Import player classes
        p1 = PlayerWrapper('player 1', options.player1_loc,
                time_limit=options.time, space_limit=options.space,
                logfn=out.comment)
        p2 = PlayerWrapper('player 2', options.player2_loc,
                time_limit=options.time, space_limit=options.space,
                logfn=out.comment)

        # We'll start measuring space usage from now, after all
        # library imports should be finished:
        set_space_line()
        # Set amount of games to be played
        MAX_GAMES = 100
        who_won = []
        for i in range(0, MAX_GAMES):
            # Play the game!
            result = play([p1, p2], ourPlayer = p1,
                    delay=options.delay,
                    logfilename=options.logfile,
                    out_function=out.comment,
                    print_state=(options.verbosity>1),
                    use_debugboard=(options.verbosity>2),
                    use_colour=options.use_colour,
                    use_unicode=options.use_unicode)
            # Display the final result of the game to the user.
            out.comment("game over!", depth=-1)
            out.print(result)
            who_won.append(result)
        print(who_won)

    # In case the game ends in an abnormal way, print a clean error
    # message for the user (rather than a trace).
    except KeyboardInterrupt:
        print() # (end the line)
        out.comment("bye!")
    except IllegalActionException as e:
        out.comment("game error!", depth=-1)
        out.print("error: invalid action!")
        out.comment(e)
    except ResourceLimitException as e:
        out.comment("game error!", depth=-1)
        out.print("error: resource limit exceeded!")
        out.comment(e)
    # If it's another kind of error then it might be coming from the player
    # itself? Then, a traceback will be more helpful.

if __name__ == '__main__':
    main()
