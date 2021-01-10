from packages.basics import *
from packages.data.values import *
from packages.data.id import *
import threading as th
import socket as sk
import typing
import base64
import arsa
import abc
import ast


class _Flag(basics.ContainerClass.Flag):
    def __init__(self, flag: str, sys: bool, value: str = None):
        super().__init__('')
        self._string: str = flag
        if sys:
            self.__flag: str = self._string
        else:
            self.__flag: str = base64.b64encode(flag.encode()).decode()
        self._value = value

    def __str__(self) -> str:
        return self._string

    @staticmethod
    def read_flag(flag: str) -> basics.ContainerClass.Flag:
        flag_in_paras = TextClass.split(flag, ':')
        flag = flag_in_paras[0]
        sys = flag.startswith('\\') and flag.startswith('\\')
        if not sys:
            flag = base64.b64decode(flag.encode()).decode()
        if len(flag_in_paras) > 1:
            return _Flag(flag, sys, value=flag_in_paras[1])
        else:
            return _Flag(flag, sys)

    def get_flag(self) -> str:
        return self.__flag


class Flags(object):
    KILL: _Flag = _Flag('\\kill\\', sys=True)
    RESULT_NO_NEED: _Flag = _Flag('\\res_no_need\\', sys=True)
    RESULT: _Flag = _Flag('\\res\\', sys=True)
    TO_PID: _Flag = _Flag('\\to_pid\\', sys=True)
    FROM_PID: _Flag = _Flag('\\from_pid\\', sys=True)

    def __init__(self, flags: typing.Union[tuple, list]):
        if type(flags) == tuple:
            self.__flags = list(flags)
        else:
            self.__flags = flags
        if len(flags) > 0:
            if type(flags[0]) == str:
                self.__flags.clear()
                for f in flags:
                    self.__flags.append(_Flag.read_flag(f))
            elif type(flags[0]) == ContainerClass.Flag or type(flags[0]) == _Flag:
                pass
            else:
                raise TypeError('unexpected type of flag: ', type(flags[0]))
        else:
            self.__flags = []

    def __contains__(self, flag: ContainerClass.Flag) -> bool:
        return flag in self.__flags

    def __str__(self) -> str:
        if self.__flags is None or len(self.__flags) == 0:
            return ''
        else:
            string = ''
            for flag in self.__flags:
                string += flag.get_flag() + '#'
            return string[:-1]

    def __len__(self) -> int:
        return len(self.__flags)

    def __iter__(self):
        return iter(self.__flags)

    def append(self, flag: ContainerClass.Flag):
        if self.__flags is None:
            raise AccidentEvents.EmptyParamError('flags are None')
        self.__flags.append(flag)


class _MessageQueue(object):
    @staticmethod
    class Message(object):
        def __init__(self, mid: ID.MID, msg: str, flags: Flags):
            self.mid, self.msg, self.flags = mid, msg, flags

    def __init__(self):
        self.__message_queue = basics.StructureClass.Queue()

    def __len__(self) -> int:
        return len(self.__message_queue)

    def current(self) -> Message:
        return self.__message_queue.get()

    def next(self) -> Message:
        self.__message_queue.remove()
        return self.__message_queue.get()

    def add(self, msg: str, flags: Flags) -> ID.MID:
        mid = ids_manager.new_mid()
        message = _MessageQueue.Message(mid, msg, flags)
        self.__message_queue.add(message)
        return mid


class Process(th.Thread):
    FLAG_RESULT_NO_NEED = Flags.RESULT_NO_NEED

    @staticmethod
    class ActionCallback(object):
        @abc.abstractmethod
        def on_send(self):
            pass

        @abc.abstractmethod
        def on_sent(self):
            pass

        @abc.abstractmethod
        def on_recved(self, result: str):
            pass

    def _on_start(self):
        pass

    def _on_started(self):
        pass

    def _on_create(self):
        pass

    def _on_created(self):
        pass

    def _on_send(self, mid):
        pass

    @abc.abstractmethod
    def _on_sent(self, mid: ID.MID):
        pass

    @abc.abstractmethod
    def _on_recved(self, mid: ID.MID, result: str):
        pass

    @abc.abstractmethod
    def _process(self, msg: str) -> str:
        pass

    def _on_kill(self):
        pass

    def _on_killed(self):
        pass

    def __init__(self, server_ip: str, server_port: int, key_length: int = None):
        """
        :param server_ip
        :type = str
        The IP of the server.

        :param server_port
        :type = list
        The ports of the server, which should be format as [main, ipidIA, backup].

        :param key_length
        :type = int
        The binary length of the rsa keys using in the connection. It's 2048 in default.
        """
        super(Process, self).__init__()
        self.__server_ip = server_ip
        self.__server_port = server_port
        self.__key_length: int = key_length
        self.__socket: sk.socket = sk.socket()
        self.__message_queue: _MessageQueue = _MessageQueue()
        self.__is_alive = False
        self.__is_closed = False
        self.__specific_action_callbacks: dict = {}
        self.__rsa_on = True
        self.__public_key, self.__private_key = None, None
        self.__remote_public_key = None
        self.__remote_info = {}
        self.__flags = []

        self.__current_message_is_specific = False

        if key_length is None:
            self.__key_length = values.get('key_length')

    def set_specific_action_callback(self, mid: ID.MID, action_callback: ActionCallback):
        self.__specific_action_callbacks[mid] = action_callback

    def set_rsa_on(self, rsa_on: bool):
        self.__rsa_on = rsa_on

    def __recv_listener(self):
        while not self.__is_closed:
            while self.__is_alive:
                msg = self.__recv()
                msg_in_paras = basics.TextClass.split(msg, '@')
                flags: Flags = Flags(tuple(basics.TextClass.split(msg_in_paras[0], '#')))
                msg = base64.b64decode(msg_in_paras[1]).decode()

                for flag in iter(flags):
                    if flag == Flags.FROM_PID:
                        self.__flags.append(_Flag('\\to_ip\\', sys=True, value=flag.get_value()))

                if Flags.KILL in flags:
                    self.__is_alive = False
                    return
                if Flags.RESULT in flags:
                    mid = self.__message_queue.current().mid

                    if mid in self.__specific_action_callbacks.keys():
                        self.__specific_action_callbacks[mid].on_recved(msg)
                    else:
                        self._on_recved(mid, msg)

                    next_msg = self.__message_queue.next()
                    if next_msg is not None:
                        self.__send(next_msg.mid, next_msg.msg, next_msg.flags)
                else:
                    send_msg = self._process(msg)
                    if Flags.RESULT_NO_NEED not in flags:
                        self.__send(ID.MID('-2'), send_msg, Flags((Flags.RESULT,)))

    def __determine_parameters(self):
        pubkey = str(self.__public_key)
        basics.NetClass.send(self.__socket, b'{\"name\": \"' + values.get('name').encode() + b'\", \"project\": \"' +
                             values.get('project').encode() + b'\"}@' + pubkey.encode('utf8') + b'#' +
                             str(self.__public_key.get_key_length()).encode('utf8'))
        msg = basics.NetClass.recv(self.__socket)
        msg_in_paras = basics.TextClass.split(msg, b'@')
        remote_info = msg_in_paras[0].decode()
        self.__remote_info = ast.literal_eval(remote_info)
        print(self.__remote_info['process_id'])
        r_pubkey = basics.TextClass.split(msg_in_paras[1], b'#')
        self.__remote_public_key = arsa.APublicKey.import_public_key(r_pubkey[0], int(r_pubkey[1].decode()))

    def run(self):
        self.__recv_listener()

    def start(self) -> None:
        """
            Use this method to make the Connection object come to life.
        """
        self._on_create()
        if self.__rsa_on:
            keys = arsa.new_keys(self.__key_length)
            self.__public_key = keys.get_public_key()
            self.__private_key = keys.get_private_key()

        self._on_created()
        self._on_start()

        try:
            self.__socket.connect((self.__server_ip, self.__server_port))
        except ConnectionRefusedError:
            raise ConnectionRefusedError('cannot establish the connection because the connection was refused')

        self.__determine_parameters()

        self.__is_alive = True
        self._on_started()
        super(Process, self).start()

    def __send(self, mid: ID.MID, msg: str, flags: Flags):
        lock = th.Lock()
        lock.acquire()
        try:
            self._on_send(mid)

            flags = flags
            for flag in self.__flags:
                flags.append(flag)

            self.__flags.clear()

            msg = arsa.encrypt(str(flags) + '@' + base64.b64encode(msg.encode()).decode(), self.__remote_public_key)
            msg = arsa.sign(msg, self.__private_key) + b'@' + msg
            basics.NetClass.send(self.__socket, msg)

            if Flags.RESULT_NO_NEED in flags:
                self.__message_queue.next()

            self._on_sent(mid)
        finally:
            lock.release()

    def __recv(self):
        msg = basics.NetClass.recv(self.__socket)
        msg_in_paras = basics.TextClass.split(msg, b'@')
        if not arsa.verify(msg_in_paras[1], msg_in_paras[0], self.__remote_public_key):
            raise basics.AccidentEvents.InvalidSignatureError('invalid signature')
        return arsa.decrypt(msg_in_paras[1], self.__private_key)

    def to(self, pid: ID.PID):
        self.__flags.append(_Flag('\\to_ip\\', sys=True, value=str(pid)))

    def send(self, msg: str, flags=None) -> ID.MID:
        """
        Use this method to send a message to S.
        The message won't be sent immediately unless the message queue is empty.
        Generally, the message will be sent after the last message has gotten its return.

        :param msg
        :type = str
        :param flags
        :type = Flag
        The message to be sent.
        """
        if not self.__is_alive:
            raise SystemError('cannot send through a dead connection')
        if msg == '':
            raise basics.AccidentEvents.EmptyMessageError('cannot send a message which is empty')

        if flags is None:
            flags = []
        else:
            flags = list(flags)

        mid = self.__message_queue.add(msg, Flags(tuple(flags)))

        if len(self.__message_queue) == 1:
            self.__send(mid, msg, self.__message_queue.current().flags)

        return mid

    def __kill(self):
        self.__send(ID.MID('-2'), '@', Flags((Flags.KILL, )))

        while self.__is_alive:
            pass

        self._on_killed()

    def kill(self):
        """
        Use this method to kill the Connection object
        """
        if not self.__is_alive:
            raise SystemError('cannot kill a dead object')
        self._on_kill()

        th.Thread(target=self.__kill).start()

    def close(self):
        """
        Use this method to close the Connection object.
        """
        if self.__is_alive or self.__is_closed:
            raise SystemError('cannot close a connection which has already been closed or still alive')

        values.save()
        self.__is_closed = True
