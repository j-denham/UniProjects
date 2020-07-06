import numpy as np
import random as rand
from FledglingAI import MinMaxSearch


EXPLOSION_CHECK_MAX_VALUE = 2

class Player:
    """ Represents the agent that plays the game
        Attributes: colour: String, enumerated to 1 for friendly, 0 otherwise
                    stack: Int, 0 for empty squares
                    piece: (stack, colour)
                    state: np.array(pieces) / 8x8x2 Matrix
                    """

    def __init__(self, colour):
        """
        This method is called once at the beginning of the game to initialise
        your player. You should use this opportunity to set up your own internal
        representation of the game state, and any other information about the
        game state you would like to maintain for the duration of the game.
        The parameter colour will be a string representing the player your
        program will play as (White or Black). The value will be one of the
        strings "white" or "black" correspondingly.
        """
        # TODO: Set up state representation.
        self.colour = colour
        self.currState = self.initState()
        # self.neuralNet = None
        self.minMax = MinMaxSearch.MinMaxSearch(self, self.colour, self.currState)
        # Initial depth
        self.searchDepth = 1


    def action(self):
        """
        This method is called at the beginning of each of your turns to request
        a choice of action from your program.
        Based on the current state of the game, your player should select and
        return an allowed action to play on this turn. The action must be
        represented based on the spec's instructions for representing actions.
        """
        # TODO: Decide what action to take, and return it
        (best, eval) = self.minMax.minimaxAlgorithm(self.currState, self.searchDepth, True, np.NINF, np.PINF)
        # When no good boom moves, move randomly
        if eval <= 0:
            moves = self.moves(self.currState, True, self.colour)

            stackMoves = []
            for move in moves:
                if self.isMoveStack(move):
                    stackMoves.append(move)

            if len(stackMoves) > 0:
                choice = rand.randint(0, len(stackMoves)-1)
                best = stackMoves[choice]
            else:
                choice = rand.randint(0, len(moves)-1)
                best = moves[choice]

        return best
 
    def isMoveStack(self, action):
        if action[0] == "MOVE":
            if action[3][1] == 1:
                return True
        return False

    def update(self, colour, action):
        """
        This method is called at the end of every turn (including your player’s
        turns) to inform your player about the most recent action. You should
        use this opportunity to maintain your internal representation of the
        game state and any other information about the game you are storing.
        The parameter colour will be a string representing the player whose turn
        it is (White or Black). The value will be one of the strings "white" or
        "black" correspondingly.
        The parameter action is a representation of the most recent action
        conforming to the spec's instructions for representing actions.
        You may assume that action will always correspond to an allowed action
        for the player colour (your method does not need to validate the action
        against the game rules).
        """
        self.currState = self.result(colour, action, self.currState)
        if self.searchDepth == 1:
            boardConfig = self.generateBoardConfig(self.currState)
            if len(boardConfig) <= 12:
                self.searchDepth = 2
            elif len(boardConfig) <= 6:
                self.searchDepth = 3
            elif len(boardConfig) <= 3:
                self.searchDepth = 4

        return


    def initState(self):
        starting_shape = np.zeros((2,2,2), dtype=np.int8)
        starting_shape[:,:,0] += 1

        # Initialise full board
        board = np.zeros((8,8,2), dtype=np.int8)

        # White at bottom of the board, black at top
        sides = [0, 6]

        # Make sure to initialise enemy side first
        if self.colour == "white": sides.reverse()

        for i in sides:
            for j in range(0, 7, 3):
                board[j:j+starting_shape.shape[0],
                      i:i+starting_shape.shape[1]] = starting_shape
            starting_shape[:,:,1] += 1

        return board

    def result(self, colour, action, state):

        # Return Successor state, given any action and parent state

        if action[0] == "MOVE":
            n, prev, to = action[1], action[2], action[3]
            newState = np.copy(state)
            newState[prev[0], prev[1], 0] -= n
            newState[to[0], to[1], 0] += n
            if colour == self.colour:
                newState[to[0], to[1], 1] = 1
                if newState[prev[0], prev[1], 0] <= 0:
                    newState[prev[0], prev[1], 1] = 0
            return newState

        elif action[0] == "BOOM":
            # Recursive Implementation
            newState = np.copy(state)
            boardConfig = self.generateBoardConfig(state)
            explosion = self.getExplosion([state[action[1][0], action[1][1], 0], action[1][0], action[1][1]], boardConfig, [])
            self.updateStateAfterExplosion([state[action[1][0]][action[1][1], 0], action[1][0], action[1][1]], explosion, newState)

            return newState

    def generateBoardConfig(self, state):
        boardConfig = []
        for i in range(8):
            for j in range(8):
                if state[i, j, 0] >= 1:
                    boardConfig.append([state[i, j, 0], i, j])

        return boardConfig

    def isEnemyPiece(self, currentPos, state):
        if state[currentPos[1], currentPos[2], 1] == 0 and state[currentPos[1], currentPos[2], 0] > 0:
            return True
        return False

    def isFriendlyPiece(self, currentPos, state):
        if state[currentPos[1], currentPos[2], 1] == 1 and state[currentPos[1], currentPos[2], 0] > 0:
            return True
        return False

    def getExplosion(self, currentPos, boardConfig, explodedPositions):
        xdir = [-1, 0, 1]
        ydir = [-1, 0, 1]
        currentBoardConfig = boardConfig
        if currentPos in currentBoardConfig:
            currentBoardConfig.remove(currentPos)

        currentExplodedPositions = explodedPositions
        foundPiece = False

        for x in xdir:
            for y in ydir:
                if not (x == 0 and y == 0):
                    for pos in boardConfig:
                        if ((currentPos[1] + x == pos[1]) and (currentPos[2] + y == pos[2])):
                            foundPiece = True
                            currentBoardConfig.remove(pos)
                            currentExplodedPositions.append(pos)
                            self.getExplosion(pos, currentBoardConfig, currentExplodedPositions)

        if foundPiece == False:
            return None
        else:
            return currentExplodedPositions

    def updateStateAfterExplosion(self, currentPos, explosion, state):
        state[currentPos[1], currentPos[2]] = [0, 0]
        if explosion is not None:
            for position in explosion:
                state[position[1], position[2]] = [0, 0]

    def moves(self, state, passive, colour):
        # Generates all valid actions for a given state and player

        # Get locations of friendly or enemy pieces
        if colour == self.colour:
            coords = np.where(state[:,:,1] == 1)
        else:
            coords = np.where((state[:,:,0] > 0) & (state[:,:,1] == 0))
        stacked = np.stack(coords, axis=1)
        moves = []
        # Booms first as low overhead to check, and are the moves that make direct
        # impact
        if passive is False:
            for row in stacked:
                moves.append(("BOOM", (row[0], row[1])))
        for row in stacked:
            size = state[row[0], row[1], 0]
            # Append furthrest moves first for economy of movement
            for i in range(size, 0 ,-1):
                # Singular pieces first to avoid disproportionate losses
                for n in range(1, size+1):
                    for x in [row[0]-i, row[0]+i]:

                        # Check move out of bounds
                        if x < 0 or x > 7: continue
                        # Check if occupied by enemy
                        if (self.currState[x, row[1], 0] != 0) and (self.currState[x, row[1], 1] == 0):
                            continue

                        if n <= EXPLOSION_CHECK_MAX_VALUE - 1:

                            boardConfig = self.generateBoardConfig(state)
                            if size - n == 0:
                                if [size, row[0], row[1]] in boardConfig:
                                    boardConfig.remove([size, row[0], row[1]])

                            explosion = self.getExplosion([1, x, row[1]], boardConfig, [])
                            if explosion is not None and len(explosion) >= n:
                                moves.append(("MOVE", n, (row[0], row[1]), (x, row[1])))

                            continue

                        moves.append(("MOVE", n, (row[0], row[1]), (x, row[1])))

                    for y in [row[1]-i, row[1]+i]:
                        # Check if out of bounds
                        if y < 0 or y > 7: continue
                        # Check if occupied by enemy
                        if (self.currState[row[0], y, 0] != 0) and (self.currState[row[0], y, 1] == 0):
                            continue

                        if n <= EXPLOSION_CHECK_MAX_VALUE - 1:

                            boardConfig = self.generateBoardConfig(state)
                            if size - n == 0:
                                if [size, row[0], row[1]] in boardConfig:
                                    boardConfig.remove([size, row[0], row[1]])

                            explosion = self.getExplosion([1, row[0], y], boardConfig, [])
                            if explosion is not None and len(explosion) >= n:
                                moves.append(("MOVE", n, (row[0], row[1]), (row[0], y)))

                            continue

                        moves.append(("MOVE", n, (row[0], row[1]), (row[0], y)))
        return moves
