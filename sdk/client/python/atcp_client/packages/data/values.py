import ast
import os


class Values(object):
    def __init__(self):
        if os.path.exists('__atcp_configurations__/configuration.py'):
            from __atcp_configurations__ import configuration
            self.__values = configuration.configuration
        else:
            self.__values = {}

    def __str__(self) -> str:
        return str(self.__values)

    def save(self):
        with open('__atcp_configurations__/configuration.py', 'w') as configuration_handler:
            configuration_handler.write('configuration = ' + str(self.__values))

    def __contains__(self, key):
        return key in self.__values.keys()

    def put(self, key: str, value):
        self.__values[key] = value

    def get(self, key: str):
        return self.__values[key]


values = Values()
