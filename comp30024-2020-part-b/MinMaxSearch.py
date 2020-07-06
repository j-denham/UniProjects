import numpy as np
from FledglingAI import player
import random
import math

STANDARDISER = 10

class MinMaxSearch:

    def __init__(self, player, colour, initialState):

        self.playerFunctions = player
        self.ourTeamColour = colour
        self.initialState = initialState

    def getNewStatesFromCurrState(self, isMaxMove, currState, colour):
        newStates = []
        moves = self.playerFunctions.moves(currState, False, colour)

        for action in moves:
            newState = self.playerFunctions.result(colour, action, currState)
            newStates.append((action, newState))

        return newStates


    def evaluateState(self, state):
        currentStateRatio = 0
        currentEnemyPieces = 0
        currentFriendlyPieces = 0
        newStateRatio = 0
        newEnemyPieces = 0
        newFriendlyPieces = 0

        for x in range(8):
            for y in range(8):
                #check initialState pieces for ratio
                if self.playerFunctions.isEnemyPiece([0, x, y], self.initialState):
                    currentEnemyPieces += 1
                elif self.playerFunctions.isFriendlyPiece([0, x, y], self.initialState):
                    currentFriendlyPieces += 1

                #check newState pieces for ratio
                if self.playerFunctions.isEnemyPiece([0, x, y], state):
                    newEnemyPieces += 1
                elif self.playerFunctions.isFriendlyPiece([0, x, y], state):
                    newFriendlyPieces += 1

        if currentEnemyPieces == 0 or newEnemyPieces == 0:
            return math.inf
        currentStateRatio = currentFriendlyPieces / currentEnemyPieces
        newStateRatio = newFriendlyPieces / newEnemyPieces
        evaluation = (newStateRatio - currentStateRatio) * STANDARDISER

        return evaluation

    def terminalTest(self, currState):
        friendlyFound = False
        enemyFound = False
        for i in range(8):
            for j in range(8):
                if (currState[i][j][0] > 0):
                    if (currState[i][j][1] == 1):
                        friendlyFound = True
                    else:
                        enemyFound = True

        if friendlyFound and enemyFound:
            return False
        return True


    def minimaxAlgorithm(self, currState, depth, isMaxMove, alpha, beta):
        if depth == 0 or self.terminalTest(currState) is True:
            return (None, self.evaluateState(currState))

        # MaxMove will always be true on our player's turn
        if isMaxMove is True:
            colour = self.ourTeamColour
        else:
            colour = self.getEnemyColour()

        newStates = self.getNewStatesFromCurrState(isMaxMove, currState, colour)
        best = None

        if isMaxMove:
            maxEval = -math.inf
            for (action, newState) in newStates:
                eval = self.minimaxAlgorithm(newState, depth - 1, False, alpha, beta)[1]
                if eval > maxEval:
                    maxEval = eval
                    best = action
                if maxEval >= beta:
                    return (best, maxEval)
                alpha = max(alpha, maxEval)
            #print("max best: ", best)
            #print("eval:", maxEval)
            return (best, maxEval)
        else:
            minEval = math.inf
            for (action, newState) in newStates:
                eval = self.minimaxAlgorithm(newState, depth-1, True, alpha, beta)[1]
                if eval < minEval:
                    minEval = eval
                    best = action
                if minEval <= alpha:
                    return (best, minEval)
                beta = min(beta, minEval)
            #print("min_best:", best)
            return (best, minEval)

    def getEnemyColour(self):
        if self.ourTeamColour == "white":
            return "black"
        else:
            return "white"
