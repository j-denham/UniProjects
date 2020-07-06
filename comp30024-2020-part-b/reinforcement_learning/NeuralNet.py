import numpy as np
import tensorflow as tf
from tensorflow.keras.models import Sequential
from tensorflow.keras.layers import InputLayer, Input
from tensorflow.keras.layers import Reshape, MaxPooling2D
from tensorflow.keras.layers import Conv2D, Dense, Flatten
from tensorflow.keras.optimizers import SGD
from tensorflow.keras.losses import mean_squared_error
from tensorflow.keras.initializers import TruncatedNormal

class NeuralNet:

    """ Convolutional Neural Network used for reinforcement learning """

    def __init__(self, load):


        # Subject to change based on further study
        self.learning_rate = 0.02
        self.discount = 0.95


        # Initialise model using keras
        self.model = Sequential()
        # Taking input as-is at the moment, may have to change to a flattened input
        # then have a reshape layer before the input based on how we store the states
        self.model.add(InputLayer(input_shape=(8, 8, 2)))
        # Initialise random weights matrix close to 0
        initializer = TruncatedNormal(mean=0.0, stddev=0.005, seed=None)
        # kernel_size is filter dimensions (3x3 in this case)
        self.model.add(Conv2D(kernel_size=3, strides=1, filters=10, padding="valid",
                              data_format="channels_last", activation="tanh",
                              kernel_initializer=initializer, name="conv_layer"))
        # Flatten 4D convolutions into 2D for the linear layers
        self.model.add(Flatten(data_format="channels_last"))
        # tanh because -1<tanh<1
        self.model.add(Dense(10, activation="tanh", name="dense1"))
        # Output Layer, linear activation because we want negative values for bad states
        self.model.add(Dense(1, activation="linear", name= "dense2"))
        # Vanilla SGD
        optimizer = SGD(learning_rate=self.learning_rate, momentum=0.0)

        if load is True:
            self.model.load_weights("weights.h5", by_name=True)

        self.model.compile(optimizer=optimizer, loss="mean_squared_error",
                           metrics=["accuracy"])

    # Called per game played
    def train(self, game_states, final_reward):
        print("training!")
        true_values = self.discounted_values(game_states, final_reward)
        print("states shape:", game_states.shape)
        print("values shape:", true_values.shape)
        self.model.fit(x=game_states, y=true_values, epochs=1, batch_size=len(game_states))

    # Used to calculate discounted values for the actual states which the
    # NeuralNet then uses for optimisation
    def discounted_values(self, game_states, final_reward):
        true_values = np.zeros(shape=(len(game_states), 1), dtype=np.float32)
        discount = 1
        for i in range(len(game_states)-1, -1, -1):
            true_values[i, :] = discount*final_reward
            discount = discount * self.discount
        return true_values

    # Gets the model's evaluation on a single state. Will be called when evaluating
    # actions when not training
    def evaluate(self, state):
        return self.model.predict(x=state, batch_size=1)

    # Save the model's weights for later use once training is done
    def save_model(self, file_path):
        #save_model(self.model, file_path, overwrite=True, include_optimizer=True)
        self.write_weights()

    # Write learned weights to file for pure numpy CNN
    def write_weights(self):
        weights = self.model.save_weights("weights.h5", overwrite=True)
        np.save("npweights", weights)
