from packages.data.id import *
from packages.data.values import *
from packages.basics import *
from process import *


class Client(object):
    def __init__(self, process: Process):
        self.__process: Process = process
        self.__values: Values = values
        self.__ids_manager: ID.IDsManager = ids_manager

    def start(self):
        if self.__process is None:
            raise AccidentEvents.EmptyParamError('process is none')
        self.__process.start()

    def to(self, pid: ID.PID):
        self.__process.to(pid=pid)

    def send(self, msg: str, *flags: ContainerClass.Flag):
        self.__process.send(msg, flags)
