"use strict";

var io = require('socket.io').listen(4450);
var db = require('mysql').createConnection({
  host: 'localhost',
  user: 'root',
  password: '0000',
  database: 'stream',
});

var channelMap = {}; // { 'socket.email': socket }, only used to check room
var socketMap = {}; // { 'socket.id': socket }
var SUCCESS = 0;
var FAILURE = -1;

db.connect();

/*
db.query('select * from tbl_user where usr_id is null', function(err, ret) {
  console.log(ret.length);
  console.log(ret);
});
*/

io.sockets.on('connection', function(socket) {
  socketMap[socket.id] = socket;

  socket.on('signUp', function(data) {
    var email = data.email;
    var name = data.name;
    var passwd = data.passwd;
    var thumbnail = data.thumbnail;

    if (email == null) {
      socket.emit('signUp', {
        code: FAILURE,
        message: 'email is empty.',
      });
    } else if (name == null) {
      socket.emit('signUp', {
        code: FAILURE,
        message: 'name is empty.',
      });
    } else if (passwd == null) {
      socket.emit('signUp', {
        code: FAILURE,
        message: 'passwd is empty.',
      });
    }

    db.query('select 1 from tbl_user where usr_id = ?', [email], function(err, ret) {
      if (err) {
        socket.emit('signUp', {
          code: FAILURE,
          message: 'error occured when to find email.',
        });
      } else if (ret.length == 1) {
        socket.emit('signUp', {
          code: FAILURE,
          message: 'already email exists.',
        });
      } else {
        var sql = 'insert into tbl_user values (?, ?, ?, ?)';

        db.query(sql, [email, name, passwd, thumbnail], function(err, ret) {
          if (err) {
            socket.emit('signUp', {
              code: FAILURE,
              message: 'error occured when to add user account.',
            });
          } else if (ret.affectedRows == 1) {
            socket.emit('signUp', {
              code: SUCCESS,
              message: 'success to add user account.',
            });
          } else {
            socket.emit('signUp', {
              code: FAILURE,
              message: 'unknown error occured when to add user account.',
            });
          }
        });
      }
    });
  });
  socket.on('signIn', function(data) {
    var email = data.email;
    var passwd = data.passwd;

    if (email == null) {
      socket.emit('signUp', {
        code: FAILURE,
        message: 'email is empty.',
      });
    } else if (passwd == null) {
      socket.emit('signUp', {
        code: FAILURE,
        message: 'passwd is empty.',
      });
    }

    var sql = ' select 1 from tbl_user' +
              '  where usr_id = ?' +
              '    and passwd = ?';

    db.query(sql, [email, passwd], function(err, ret) {
      if (err) {
        socket.emit('signIn', {
          code: FAILURE,
          message: 'error occured when to find user account.',
        });
      } else if (ret.length == 0) {
        socket.emit('signIn', {
          code: FAILURE,
          message: 'user account not exists.',
        });
      } else {
        socket.email = email;
        socket.emit('signIn', {
          code: SUCCESS,
          message: 'success to login.',
          email: email,
        });
      }
    });
  });
  socket.on('signOut', function(data) {
    if (socket.email == null) {
      socket.emit('signOut', {
        code: FAILURE,
        message: 'current user not sign in.',
      });
    } else {
      delete socket.email;
      socket.emit('signOut', {
        code: SUCCESS,
        message: 'success to sign out.',
      });
    }
  });
  socket.on('createChannel', function(data) {
    if (socket.email == null) {
      socket.emit('createChannel', {
        code: FAILURE,
        message: 'not logged in.',
      });
    } else if (socket.hasChannel) {
      socket.emit('createChannel', {
        code: FAILURE,
        message: 'already create channel.',
      });
    } else {
      channelMap[socket.email] = socket;
      socket.hasChannel = true;
      socket.join(socket.email);
      socket.emit('createChannel', {
        code: SUCCESS,
        message: 'success to create channel',
      });
    }
  });
  socket.on('deleteChannel', function(data) {
    if (socket.email == null) {
      socket.emit('deleteChannel', {
        code: FAILURE,
        message: 'not logged in.',
      });
    } else if (!socket.hasChannel) {
      socket.emit('deleteChannel', {
        code: FAILURE,
        message: 'not has channel.',
      });
    } else {
      socket.broadcast.to(socket.email).emit({
        code: SUCCESS,
        message: 'delete channel.',
      });
      delete channelMap[socket.email];
      socket.hasChannel = false;
      socket.leave(socket.email);
      socket.emit({
        code: SUCCESS,
        message: 'success to delete channel.',
      });
    }
  });
  socket.on('enterChannel', function(data) {
    var email = data.email;
    var title = email;

    if (channelMap[email] == null) {
      socket.emit('enterChannel', {
        code: FAILURE,
        message: 'fail to enter channel.',
      });
    } else {
      socket.join(email);
      socket.channel = email;
      socket.emit('enterChannel', {
        code: SUCCESS,
        message: 'success to enter channel.',
      });
    }
  });
  socket.on('leaveChannel', function(data) {
    socket.leave(socket.channel);
    delete socket.channel;
    socket.emit('leaveChannel', {
      code: SUCCESS,
      message: 'success to leave channel.',
    });
  });
  socket.on('offer', function(data) {
    var sdp = data.sdp;

    socket.broadcast.to(socket.channel).emit('offer', {
      code: SUCCESS,
      socketId: socket.id,
      sdp: sdp,
    });
  });
  socket.on('answer', function(data) {
    var socketId = data.socketId;
    var sdp = data.sdp;

    socketMap[socketId].emit('answer', {
      socketId: socket.id,
      code: SUCCESS,
      sdp: sdp,
    });
  });
  socket.on('disconnect', function() {
    delete socketMap[socket.id];
  });
});
