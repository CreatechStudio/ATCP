from packages import basics


class ID(object):
    class MID(object):
        def __init__(self, id_: str):
            self.__id: str = id_

        def __str__(self):
            return self.__id

        def __eq__(self, other):
            if self == other:
                return True
            if other is None or self.__class__ != other.__class__:
                return False
            return self.__id == other.__id

        def __hash__(self):
            return hash(self.__id)

    class PID(object):
        def __init__(self, id_: str):
            self.__id: str = id_

        def __str__(self):
            return self.__id

        def __eq__(self, other):
            if self == other:
                return True
            if other is None or self.__class__ != other.__class__:
                return False
            return self.__id == other.__id

        def __hash__(self):
            return hash(self.__id)

    class IDsManager(object):
        def __init__(self):
            self.__max_mid: int = -1

        def new_mid(self):
            self.__max_mid += 1
            return ID.MID(str(self.__max_mid))


ids_manager = ID.IDsManager()
