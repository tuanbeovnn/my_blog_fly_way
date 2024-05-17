const postData = require('./axiosClient');
const generateMockData = require('./postTemplate');

async function main() {
    const args = process.argv.slice(2);
    if (args.length === 3 && args[0] === 'Post') {
        const url = args[1];
        const token = args[2];
        const { posts } = generateMockData();

        for (const post of posts) {
            await postData(url, post, token);
        }
    } else {
        console.log('Usage: node generateTestData Post <URL> <Token>');
    }
}

main();
