"use strict";

var io = require('socket.io').listen(4450);
var db = require('mysql').createConnection({
  host: 'localhost',
  user: 'root',
  password: '0000',
  database: 'stream',
});

var channels = {}; // { email : { title: '', users: ['', ...] }, ... }
var SUCCESS = 0;
var FAILURE = -1;

db.connect();
io.sockets.on('connection', function(socket) {
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
        var sql = 'insert into tbl_user values (?, ?, md5(concat("prefix", ?)), ?)';

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
              '    and passwd = md5(concat("prefix", ?))';

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
    var email = data.email;
    var title = data.title;

    if (socket.channel != null) {
      // TODO already has channel
    }
    // TODO
  });
  socket.on('removeChannel', function(data) {
    // TODO
  });
  socket.on('enterChannel', function(data) {
    var email = data.email;
    var title = data.titie;

    if (channels[email] == null) {
      
    }
  });
  socket.on('leaveChannel', function(data) {
  });
});
