import numpy as np
import tensorflow as tf
import random as rand


class GameStateMemory:

    " Receives states from GameStateMemory to store and feed to NeuralNet"

    def __init__(self, size):

        # Array for the previous states of the game-environment.
        self.state_shape = [8, 8, 2]

        self.states = []

        # Capacity of the replay-memory as the number of states.
        self.size = size

        # Reset the number of used states in the replay-memory.
        self.num_used = 0

        # Rewards observed for each of the states in the memory.
        self.rewards = np.zeros(shape=size, dtype=np.float)

    def is_full(self):
        """Return boolean whether the replay-memory is full."""
        return self.num_used == self.size

    def add(self, state, reward):
        """
        Add an observed state from the game-environment

        :param state:
            Current state of the game-environment.

        :param reward:
            Whether this state is a win (1) loss (-1) or neither (0).

        """

        if not self.is_full():
            # Index into the arrays for convenience.
            k = self.num_used

            # Increase the number of used elements in the replay-memory.
            self.num_used += 1

            # Store all the values in the replay-memory.
            self.states.append(state)

            # Note that the reward is limited. This is done to stabilize
            # the training of the Neural Network.
            self.rewards[k] = np.clip(reward, -1.0, 1.0)

    def output_states(self):
        output_states = np.zeros(shape=[self.num_used] + self.state_shape, dtype=np.int8)
        k = 0
        for i in self.states:
            output_states[k, :, :, :] = i
            k += 1
        print("Output Shape: ", output_states.shape)
        return output_states

    def reset_memory(self):
        self.states = []
        self.num_used = 0
