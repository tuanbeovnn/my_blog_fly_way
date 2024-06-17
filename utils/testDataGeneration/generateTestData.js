const {processPosts, processComments} = require('./dataGeneration');
const {postData, getCategoryList, commentsData} = require('./axiosClient');
const postTemplate = require('./postTemplate.json');
const commentTemplate = require('./commentTemplate.json');

const main = async () => {
    const args = process.argv.slice(2);
    let action = 'test';

    if (args.length >= 3) {
        action = args[0];

        const url = args[1];
        const token = args[2];

        if (action === 'Post') {
            await processPosts(url, token, getCategoryList, postData, postTemplate);
        } else if (action === 'Comment') {
            await processComments(url, token, commentsData, commentTemplate);
        } else {
            console.log('Usage: node generateTestData Post <URL> <Token> [test|production]');
        }
    } else {
        console.log('Usage: node generateTestData Post <URL> <Token> [test|production]');
    }
};

main();