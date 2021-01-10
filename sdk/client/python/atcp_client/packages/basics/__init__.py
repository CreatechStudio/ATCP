import socket as sk
import threading as th
import base64

_NetClass_last_message: bytes = b''


class AccidentEvents(object):
    class AccidentEvent(RuntimeError):
        def __init__(self, *args):
            self.__args = args

        def __str__(self):
            return 'AccidentEvent{' + \
                   'args=' + str(self.__args) + \
                   '}'

    class EmptyMessageError(AccidentEvent):
        def __init__(self, *args):
            super().__init__(args)

    class EmptyParamError(AccidentEvent):
        def __init__(self, *args):
            super().__init__(args)

    class InvalidSignatureError(AccidentEvent):
        def __init__(self, *args):
            super().__init__(args)


class TextClass(object):
    @staticmethod
    def split(content, target):
        res = []
        content_len = len(content)
        target_len = len(target)
        from_index = 0
        for i in range(content_len - target_len + 1):
            if content[i: i + target_len] == target:
                res.append(content[from_index: i])
                from_index = i + target_len
        if from_index != content_len:
            res.append(content[from_index:])
        if len(res) == 0:
            return ['']

        return res


class NetClass(object):
    @staticmethod
    def recv(socket: sk.socket) -> bytes:
        global _NetClass_last_message
        msg = b''

        if _NetClass_last_message != b'':
            index: int = _NetClass_last_message.find(b'\\\\\\')
            if index != len(_NetClass_last_message) - 3:
                msg = _NetClass_last_message[:index + 3]
                _NetClass_last_message = _NetClass_last_message[index:]
            else:
                msg = _NetClass_last_message[:index]
                _NetClass_last_message = b''
            return msg
        while True:
            msg += socket.recv(10240)
            if msg.endswith(b'\\\\\\'):
                break
        if len(msg) < 3:
            return b''
        index: int = msg.find(b'\\\\\\')
        if index != len(msg) - 3:
            _NetClass_last_message = msg[index + 3:]
            return msg[:index]
        return msg[:len(msg) - 3]

    @staticmethod
    def send(socket: sk.socket, msg: bytes):
        socket.send(msg + b'\\\\\\')


class StructureClass(object):
    @staticmethod
    class Queue(object):
        def __init__(self):
            self.__queue = []
            self.__used_length = 0

        def __len__(self):
            return self.__used_length

        def get(self):
            if len(self.__queue) == 0:
                return None
            return self.__queue[0]

        def add(self, *element):
            lock = th.Lock()
            lock.acquire()
            try:
                for e in element:
                    self.__queue.append(e)
                self.__used_length += 1
            finally:
                lock.release()

        def remove(self):
            lock = th.Lock()
            lock.acquire()
            try:
                self.__queue = self.__queue[1:]
                self.__used_length -= 1
            finally:
                lock.release()


class ContainerClass(object):
    @staticmethod
    class Flag(object):
        def __init__(self, flag: str, value: str = None):
            if flag == '':
                self._string = self.__flag = ''
            else:
                self._string: str = flag
                self.__flag: str = base64.b64encode(flag.encode()).decode()
            self._value = value

        def __str__(self) -> str:
            if self._value is None:
                return self._string
            else:
                return self._string + ':' + self._value

        def __eq__(self, other):
            if self is other:
                return True
            if other is None or type(self) != type(other):
                return False
            other: ContainerClass.Flag = other
            return self.__flag == other.__flag

        def __hash__(self):
            return hash(self.get_flag())

        def get_flag(self) -> str:
            if self._value is None:
                return self.__flag
            else:
                return self.__flag + ':' + self._value

        def get_value(self) -> str:
            return self._value
